# Devices / Appointments / Payments → Supabase (Write-Through Plug-In)

## Goal

After the customer takes a photo and the AI returns the part-analysis result, the **device details, the linked appointment, and the linked payment** are persisted to Supabase. They survive app restarts, are visible to staff on the same Supabase project, and the existing in-memory data flow keeps working unchanged.

**Photo bytes**: NOT saved (per your clarification). The AI's part list, device name, category, and recovery value are saved as the device's `accessories`, `device_name`, `category`, `recovery_value` columns — that's the full AI output captured.

**Existing logic preserved**: every UI screen, every ViewModel, the cross-store `CustomerDataStore ↔ StaffDataStore` sync, the in-memory `devices/appointments/payments` lists — all stay intact. We just add write-through side effects and a one-time loader.

---

## Step 1 — SQL migration (one-time, in Supabase dashboard)

Three new tables, all referencing `public.users(id)` as `customer_id`. RLS off (matches the existing `users` table), anon can select/insert/update.

```sql
create table public.devices (
  id               uuid primary key default gen_random_uuid(),
  device_id        text unique not null,                  -- "DEV001"
  customer_id      uuid not null references public.users(id),
  customer_name    text not null,
  device_name      text not null,
  category         text not null,
  date             text not null,
  status           text not null default 'Pending',
  accessories      jsonb not null default '[]'::jsonb,    -- [{name, price}, ...]
  recovery_value   double precision not null,
  created_at       timestamptz not null default now(),
  updated_at       timestamptz not null default now()
);

create table public.appointments (
  id              uuid primary key default gen_random_uuid(),
  appointment_id  text unique not null,                   -- "APP001"
  customer_id     uuid not null references public.users(id),
  device_id       text not null,                          -- matches devices.device_id
  device_name     text not null,
  date            text not null,
  time            text not null,
  company         text not null,
  location        text not null,
  status          text not null default 'Scheduled',
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);

create table public.payments (
  id              uuid primary key default gen_random_uuid(),
  payment_id      text unique not null,                   -- "PAY001"
  customer_id     uuid not null references public.users(id),
  appointment_id  text,                                   -- nullable; matches appointments.appointment_id
  device_id       text not null,
  device_name     text not null,
  amount          double precision not null,
  date            text not null,
  method          text not null default 'Bank Transfer',
  status          text not null default 'Pending',
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);

grant select, insert, update on public.devices      to anon;
grant select, insert, update on public.appointments to anon;
grant select, insert, update on public.payments     to anon;

create index devices_customer_idx      on public.devices      (customer_id);
create index appointments_customer_idx on public.appointments (customer_id);
create index payments_customer_idx     on public.payments     (customer_id);
```

Notes:
- `device_id` / `appointment_id` / `payment_id` are the existing "DEV001"-style strings already used throughout the app. Keeping them as-is means no renumbering.
- `uuid` PKs are internal Supabase row IDs that we never reference in the app.
- `accessories` is JSONB: `[{"name": "Battery", "price": 80.0}, ...]`. The list is also already in the in-memory `CustomerDevice.accessories` field.
- `customer_name` is denormalized so staff can see whose device it is without joining `users`.

---

## Step 2 — New Kotlin files

### `data/db/DeviceRow.kt`, `AppointmentRow.kt`, `PaymentRow.kt`

`@Serializable` data classes matching the DB schema (with `@SerialName("snake_case")` for column-name mapping). Same pattern as `UserRow` from the auth work. One new `NewDeviceRow` (without internal `id`) for inserts.

### `data/db/DeviceRepository.kt`

```kotlin
class DeviceRepository(private val postgrest: Postgrest) {
    suspend fun insertDevice(d: NewDeviceRow): DeviceRow
    suspend fun updateStatus(deviceId: String, status: String): Boolean
    suspend fun listByCustomer(customerId: String): List<DeviceRow>
    suspend fun listAll(): List<DeviceRow>
}
object DeviceRepo { val instance = DeviceRepository(...) }
```

Same shape for `AppointmentRepository` and `PaymentRepository`. All use the same write-through-friendly pattern (insert returns the row via two-step: insert + select by unique ID; updates use the existing re-query-to-confirm approach).

`IdOnlyRow` from the auth layer is reused for the count checks.

---

## Step 3 — Write-through side effects

**`ui/customer/CustomerDataStore.kt`** — add at the end of `addDevice`, `addAppointment`, `addPayment`, `deleteDeviceByCustomer`, `updateDeviceStatus`, `updateAppointmentStatus`, `cancelAppointment`:

```kotlin
// Best-effort write-through. Failure here is logged but
// does NOT roll back the in-memory operation, so the UI
// keeps working even if the network is down.
try {
    runBlocking {
        DeviceRepo.instance.insertDevice(NewDeviceRow(...))
    }
} catch (t: Throwable) {
    Log.e("NekoEcoDB", "insertDevice failed", t)
}
```

The arguments use the same `newDevice` / `newAppointment` / `newPayment` values that just got added to the in-memory list. The cross-store sync to `StaffDataStore` continues unchanged — staff still get a local mirror via `addDeviceFromCustomer` etc.

`StaffDataStore.kt` — write-through in `addDeviceFromCustomer`, `updateDeviceStatus`, `createAppointmentForApprovedDevice`, `markPaymentPaid`, `completeAppointment`, `acceptAppointment`, `rejectAppointment`, `rescheduleAppointment`, `cancelAppointmentByCustomer`, `deleteDevice`. Each call site becomes:

```kotlin
devices.add(...)
try { runBlocking { DeviceRepo.instance.insertDevice(...) } } catch (...) { ... }
```

This means staff changes (Approve / Reject / Paid) also propagate to the DB.

---

## Step 4 — Loaders

**`ui/customer/CustomerDataStore.kt`** — add:
```kotlin
suspend fun loadForCustomer(customerId: String) {
    // Fetch from DB, populate devices/appointments/payments
    // in-memory lists (clear-and-refill to avoid duplicates on
    // subsequent loads).
}
```

**`ui/staff/StaffDataStore.kt`** — add:
```kotlin
suspend fun loadAll() {
    // Fetch ALL devices/appointments/payments from DB,
    // populate lists.
}
```

Loaders use `upsert-by-(device_id/appointment_id/payment_id)` logic: clear the list and refill from DB. No conflict with the write-through pattern because the write-through is "fire and forget to DB" while the loader is "pull authoritative state from DB".

---

## Step 5 — Call the loaders from MainScreen

**`ui/customer/CustomerMainScreen.kt`** — add a `LaunchedEffect(currentCustomer.customerId)` that calls `CustomerDataStore.loadForCustomer(customerId)`. Runs once per customer session.

**`ui/staff/StaffMainScreen.kt`** — add a `LaunchedEffect(Unit)` that calls `StaffDataStore.loadAll()`. Runs once per staff session.

Both use `runBlocking` inside the `LaunchedEffect` (matching the existing pattern used by `login`).

---

## Step 6 — What stays untouched

- Every Compose screen reads from `CustomerDataStore.devices` etc. directly — no change.
- `CustomerLoginViewModel`, `StaffLoginViewModel`, all other ViewModels — no change.
- `MainActivity`, navigation graph — no change.
- `CameraScreen`, `AiResultScreen`, `ScanViewModel` — no change. The AI still produces the same `ScanResult` object; we just persist it to DB instead of dropping it on app exit.
- `Gemini` integration, `RetrofitClient`, `BuildConfig.GEMINI_API_KEY` — no change.
- Auth (`UserRow`, `AuthRepository`, login/signup/forgot-password) — no change.

---

## Files

**Created (5)**:
- `data/db/DeviceRow.kt`
- `data/db/AppointmentRow.kt`
- `data/db/PaymentRow.kt`
- `data/db/DeviceRepository.kt`
- `data/db/AppointmentRepository.kt`
- `data/db/PaymentRepository.kt`

(That's 6, but they're all small — one file each, ~30-80 lines.)

**Modified (3)**:
- `ui/customer/CustomerDataStore.kt` — add `loadForCustomer` + write-through blocks (~10 small blocks at the end of existing functions).
- `ui/staff/StaffDataStore.kt` — add `loadAll` + write-through blocks (~10 small blocks).
- `ui/customer/CustomerMainScreen.kt` — add 1 `LaunchedEffect`.
- `ui/staff/StaffMainScreen.kt` — add 1 `LaunchedEffect`.

**User-edited (1, not committed)**:
- `local.properties` — already has the Supabase keys; nothing new.

**Untouched**: all Compose screens, all ViewModels, MainActivity, navigation, Gemini, camera, auth layer.

---

## Verification

1. Apply the SQL migration in the Supabase dashboard.
2. Build & run.
3. Sign up a new customer, log in.
4. Go through the camera → AI scan flow with any device. After the result screen:
   - Confirm the customer sees their new device in **Customer Devices** screen (in-memory list still populated).
   - Open Supabase Table Editor → `public.devices` → the new row appears with all fields populated.
   - Same for `public.appointments` and `public.payments`.
5. Kill the app and reopen (still as the same customer) → devices still listed (loaded from DB on screen mount).
6. Log out, log in as `ST001` / `122507` → staff sees the same device in their **Devices** tab (loaded from DB).
7. From staff, change status to **Approved** → the device row's `status` column in Supabase updates.
8. From staff, mark a payment as **Paid** → the payment row's `status` updates.

If any DB write fails (e.g. network down), the existing in-memory flow continues working — the user sees their data, and the next launch's loader picks up whatever IS in the DB. The DB is "best-effort persistence", not the strict source of truth — by design, so the app doesn't break when offline.