package com.example.nekoeco.ui.customer

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.nekoeco.data.auth.AuthRepo
import com.example.nekoeco.data.auth.PasswordHasher
import com.example.nekoeco.data.auth.SessionPreferences
import com.example.nekoeco.data.db.AppointmentRepo
import com.example.nekoeco.data.db.DeviceRepo
import com.example.nekoeco.data.db.NewAppointmentRow
import com.example.nekoeco.data.db.NewDeviceRow
import com.example.nekoeco.data.db.NewPaymentRow
import com.example.nekoeco.data.db.PaymentRepo
import com.example.nekoeco.ui.staff.StaffDataStore
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive


// ============================================================
// CUSTOMER DEVICE
// ============================================================

data class CustomerDevice(
    val deviceId: String,
    val customerId: String,
    val deviceName: String,
    val category: String,
    val date: String,
    var status: String,
    val accessories: List<CustomerAccessory>,
    val recoveryValue: Double
)


// ============================================================
// ACCESSORY
// ============================================================

data class CustomerAccessory(
    val name: String,
    val price: Double,

    // Whether the company will actually recover this
    // part (set by the staff device inspection).
    // Defaults to true so pre-inspection rows and
    // legacy JSON without the flag stay all-approved.
    val approved: Boolean = true
)


// ============================================================
// CUSTOMER
// ============================================================

data class Customer(
    val customerId: String,
    val name: String,
    val email: String,
    val password: String
)


// ============================================================
// CUSTOMER APPOINTMENT
// ============================================================

data class CustomerAppointment(
    val appointmentId: String,
    val customerId: String,
    val deviceId: String,
    val deviceName: String,
    val date: String,
    val time: String,
    val company: String,
    val location: String,
    var status: String
)


// ============================================================
// CUSTOMER PAYMENT
// ============================================================

data class CustomerPayment(
    val paymentId: String,
    val customerId: String,
    val appointmentId: String,
    val deviceName: String,
    val amount: Double,
    val date: String,
    val method: String,
    var status: String
)


// ============================================================
// CUSTOMER DATA STORE
// ============================================================

object CustomerDataStore {


    // ========================================================
    // CURRENT CUSTOMER
    // ========================================================

    var currentCustomer: Customer? = null


    // ========================================================
    // DATA VERSION
    //
    // Bumped every time the in-memory caches are mutated by
    // a load or any write-through path. Child screens that
    // maintain their own viewmodel snapshots (Home, Schedule,
    // Payments) read this counter and key their refresh
    // LaunchedEffect on it — so the moment the data store is
    // repopulated after loadForCustomer, every mounted
    // child screen re-fetches and renders correctly.
    //
    // Without this, the child screens would render with
    // whatever snapshot they took when they first mounted,
    // and would only re-render when the user switched tabs
    // (because tab-switch remounts the screen and re-fires
    // its LaunchedEffect(Unit)).
    // ========================================================

    private val _dataVersion = mutableStateOf(0)


    val dataVersion: Int
        get() = _dataVersion.value


    private fun bumpDataVersion() {

        _dataVersion.value = _dataVersion.value + 1
    }


    // ========================================================
    // COLD-LAUNCH LOADED FLAG
    //
    // True once the very first loadForCustomer (customer)
    // or loadAll (staff) has completed. Used by the
    // dashboard to show a centred spinner until the first
    // batch of data is in the in-memory caches — without
    // this, the home tab would paint with all zeros for the
    // ~1s it takes Supabase to respond.
    //
    // Stays true once flipped — every later load is a
    // background refresh and shouldn't re-show the spinner.
    // ========================================================

    private val _coldLaunchLoaded = mutableStateOf(false)


    val coldLaunchLoaded: Boolean
        get() = _coldLaunchLoaded.value


    internal fun markColdLaunchLoaded() {

        _coldLaunchLoaded.value = true
    }


    // ========================================================
    // LOGIN
    // ========================================================

    fun login(
        email: String,
        password: String
    ): Customer? {

        Log.d("NekoEcoAuth", "Customer login attempt: email=${email.trim()}")

        val row = try {

            runBlocking {

                AuthRepo.instance.findCustomerByEmailAndHash(

                    email = email.trim(),

                    hash = PasswordHasher.sha256(password)
                )
            }
        } catch (t: Throwable) {

            // ================================================
            // Catch EVERYTHING (Throwable, not Exception) so
            // nothing escapes up to the main-thread onClick
            // and kills the app. Log it for diagnosis.
            // ================================================

            Log.e("NekoEcoAuth", "Customer login failed", t)
            return null
        }


        if (row == null) {

            Log.d("NekoEcoAuth", "Customer login: no matching row")
            return null
        }


        currentCustomer =
            Customer(

                customerId = row.id,

                name = row.name,

                email = row.email ?: return null,

                password = ""
            )


        // =====================================================
        // Persist this login so a fresh app launch can skip
        // the role-selection screen and land here directly.
        // Password is never stored — only the email. The
        // launch path re-validates via AuthRepo and falls
        // back to login if the account is gone.
        // =====================================================

        val justLoggedIn = currentCustomer

        if (justLoggedIn != null) {

            SessionPreferences.persistCustomer(
                email = justLoggedIn.email,
                name = justLoggedIn.name,
                customerId = justLoggedIn.customerId
            )
        }


        Log.d("NekoEcoAuth", "Customer login OK: ${row.email}")
        return currentCustomer
    }


    // ========================================================
    // SIGN UP
    // ========================================================

    fun signUp(
        name: String,
        email: String,
        password: String
    ): Boolean {

        val cleanName = name.trim()

        val cleanEmail = email.trim()


        if (
            cleanName.isBlank() ||
            cleanEmail.isBlank() ||
            password.isBlank()
        ) {
            return false
        }


        Log.d(
            "NekoEcoAuth",
            "Signup attempt: name='${cleanName}' email='${cleanEmail}'"
        )


        // ====================================================
        // Pre-check for email uniqueness. If this throws we
        // log it and ASSUME the email does NOT exist (safer
        // to try the insert and let the DB enforce uniqueness
        // than to wrongly report "already registered").
        // ====================================================

        val alreadyExists = try {

            runBlocking {

                AuthRepo.instance.emailExists(cleanEmail)
            }
        } catch (t: Throwable) {

            Log.e("NekoEcoAuth", "emailExists check failed; assuming false", t)
            false
        }


        if (alreadyExists) {

            Log.d("NekoEcoAuth", "Signup: email already exists")
            return false
        }


        // ====================================================
        // Insert. If this throws we surface the error in
        // logcat AND return false (ViewModel displays
        // "already registered"). Once we have HTTP logging in
        // place we can see what actually went wrong.
        // ====================================================

        val row = try {

            runBlocking {

                AuthRepo.instance.insertCustomer(

                    name = cleanName,

                    email = cleanEmail,

                    hash = PasswordHasher.sha256(password)
                )
            }
        } catch (e: PostgrestRestException) {

            // ================================================
            // PostgREST returns 409 (Postgres unique_violation
            // 23505) when email is already in use. The
            // Kotlin SDK throws PostgrestRestException for
            // HTTP 4xx/5xx responses. We translate this to
            // the existing "already registered" UI text.
            //
            // Any other HTTP failure is a real error — log it
            // so we can diagnose from logcat instead of
            // silently reporting duplicate-email.
            // ================================================

            val status = e.statusCode

            if (status == 409) {

                Log.d("NekoEcoAuth", "Signup: duplicate email (409)")
                return false
            }

            Log.e("NekoEcoAuth", "Customer signup HTTP $status failed", e)
            return false
        } catch (t: Throwable) {

            Log.e("NekoEcoAuth", "Customer signup failed", t)
            return false
        }


        currentCustomer =
            Customer(

                customerId = row.id,

                name = row.name,

                email = row.email ?: return false,

                password = ""
            )


        Log.d("NekoEcoAuth", "Customer signup OK: ${row.email}")
        return true
    }


    // ========================================================
    // FORGOT PASSWORD — verify email + name, then reset
    // ========================================================

    fun verifyCustomerForPasswordReset(
        email: String,
        name: String
    ): Boolean {

        Log.d(
            "NekoEcoAuth",
            "Customer forgot-password verify: email='${email.trim()}' name='${name.trim()}'"
        )


        return try {

            runBlocking {

                AuthRepo.instance.findCustomerByEmailAndName(
                    email = email.trim(),
                    name  = name.trim()
                )
            }
        } catch (t: Throwable) {

            Log.e("NekoEcoAuth", "verifyCustomerForPasswordReset failed", t)
            false
        }
    }


    fun resetCustomerPassword(
        email: String,
        name: String,
        newPassword: String
    ): Boolean {

        if (newPassword.isBlank()) return false


        Log.d(
            "NekoEcoAuth",
            "Customer password reset attempt: email='${email.trim()}'"
        )


        return try {

            runBlocking {

                AuthRepo.instance.updateCustomerPassword(
                    email = email.trim(),
                    name  = name.trim(),
                    hash  = PasswordHasher.sha256(newPassword)
                )
            }
        } catch (t: Throwable) {

            Log.e("NekoEcoAuth", "Customer password reset failed", t)
            false
        }
    }


    // ========================================================
    // HEAL COPIED DEVICE NAMES
    //
    // Appointments and payments store a COPIED device_name.
    // If a rename ever missed a row (e.g. legacy data written
    // before the rename write-through existed), a reload
    // would pull the stale name back into Schedule and the
    // home upcoming pickups. Re-derive the display names
    // from the device record on every load.
    // ========================================================

    private suspend fun healDeviceNames(
        loadedDevices: List<CustomerDevice>,
        loadedAppointments: List<CustomerAppointment>,
        loadedPayments: List<CustomerPayment>
    ): Pair<List<CustomerAppointment>, List<CustomerPayment>> {

        val deviceNamesById =
            loadedDevices.associate {
                it.deviceId to it.deviceName
            }


        // ====================================================
        // ORPHAN CLEANUP
        //
        // Appointments whose device row no longer exists were
        // left behind by deletes from older builds — they show
        // up in Schedule with stale names forever. Remove them
        // from the loaded set AND from Supabase; their payment
        // rows go with them.
        // ====================================================

        val orphanDeviceIds =
            loadedAppointments
                .map { it.deviceId }
                .filter {

                    it.isNotBlank() &&
                            !deviceNamesById.containsKey(it)
                }
                .distinct()


        if (
            orphanDeviceIds.isNotEmpty()
        ) {

            try {

                withContext(Dispatchers.IO) {

                    orphanDeviceIds.forEach { deviceId ->

                        AppointmentRepo.instance
                            .deleteByDevice(deviceId)

                        PaymentRepo.instance
                            .deleteByDevice(deviceId)
                    }
                }

            } catch (t: Throwable) {

                Log.e(
                    "NekoEcoDB",
                    "orphan appointment cleanup failed",
                    t
                )
            }
        }


        val keptAppointments =
            loadedAppointments.filter {

                it.deviceId.isNotBlank() &&
                        deviceNamesById.containsKey(
                            it.deviceId
                        )
            }


        val keptPayments =
            loadedPayments.filter { payment ->

                payment.appointmentId.isNotBlank() &&
                        keptAppointments.any {

                            it.appointmentId ==
                                    payment.appointmentId
                        }
            }


        val healedAppointments =
            keptAppointments.map { appointment ->

                val currentName =
                    deviceNamesById[
                        appointment.deviceId
                    ]

                if (currentName != null &&
                    currentName != appointment.deviceName
                ) {

                    appointment.copy(
                        deviceName = currentName
                    )

                } else {

                    appointment
                }
            }


        val appointmentNamesById =
            healedAppointments
                .filter {
                    it.appointmentId.isNotBlank()
                }
                .associate {
                    it.appointmentId to it.deviceName
                }


        val healedPayments =
            keptPayments.map { payment ->

                val currentName =
                    appointmentNamesById[
                        payment.appointmentId
                    ]

                if (currentName != null &&
                    currentName != payment.deviceName
                ) {

                    payment.copy(
                        deviceName = currentName
                    )

                } else {

                    payment
                }
            }


        return healedAppointments to healedPayments
    }


    // ========================================================
    // LOAD FROM SUPABASE — called once on screen mount
    // ========================================================

    suspend fun loadForCustomer(
        customerId: String
    ) {

        // =====================================================
        // Fetch FIRST, then replace. If we cleared the local
        // caches before the network call, every child screen
        // that mounted in the meantime (Home, Schedule,
        // Payments, Devices) would snapshot an empty state
        // and only re-render after the user switched tabs.
        //
        // Reading flows in this app are all either:
        //   - filtered by currentCustomer.customerId, so
        //     another customer's leftover rows never leak;
        //   - or deviceId lookups, which can only match a
        //     newly-created device if the generator happens
        //     to pick the same id — the id generator scans
        //     both lists before choosing, so duplicates are
        //     not introduced.
        //
        // So replacing atomically after success is safe and
        // avoids the empty-window UX glitch.
        //
        // This is a `suspend` function so the network calls
        // below run on whatever dispatcher the caller is on
        // (the session-restore coroutine uses Dispatchers.IO).
        // The caller must dispatch the atomic-swap portion
        // back to the main thread because Compose's snapshot
        // system tracks state mutations on the main thread.
        // =====================================================


        // Build the new contents into plain local lists on
        // the IO dispatcher, then swap atomically into the
        // snapshot lists on the main thread.
        val newDevices: List<CustomerDevice>
        val newAppointments: List<CustomerAppointment>
        val newPayments: List<CustomerPayment>


        try {

            newDevices = withContext(Dispatchers.IO) {

                val deviceRows =
                    DeviceRepo.instance.listByCustomer(customerId)

                deviceRows.map { row ->

                    val accessories =
                        (row.accessories as? JsonArray)
                            ?.mapNotNull { element ->

                                val obj =
                                    element as? JsonObject
                                        ?: return@mapNotNull null

                                CustomerAccessory(
                                    name =
                                        obj["name"]
                                            ?.jsonPrimitive
                                            ?.content
                                            ?: "",
                                    price =
                                        obj["price"]
                                            ?.jsonPrimitive
                                            ?.double
                                            ?: 0.0,
                                    approved =
                                        obj["approved"]
                                            ?.jsonPrimitive
                                            ?.booleanOrNull
                                            ?: true
                                )
                            }
                            ?: emptyList()


                    CustomerDevice(
                        deviceId = row.deviceId,
                        customerId = row.customerId,
                        deviceName = row.deviceName,
                        category = row.category,
                        date = row.date,
                        status = row.status,
                        accessories = accessories,
                        recoveryValue = row.recoveryValue
                    )
                }
            }


            newAppointments = withContext(Dispatchers.IO) {

                val apptRows =
                    AppointmentRepo.instance.listByCustomer(customerId)

                apptRows.map { row ->

                    CustomerAppointment(
                        appointmentId = row.appointmentId,
                        customerId = row.customerId,
                        deviceId = row.deviceId,
                        deviceName = row.deviceName,
                        date = row.date,
                        time = row.time,
                        company = row.company,
                        location = row.location,
                        status = row.status
                    )
                }
            }


            newPayments = withContext(Dispatchers.IO) {

                val paymentRows =
                    PaymentRepo.instance.listByCustomer(customerId)

                paymentRows.map { row ->

                    CustomerPayment(
                        paymentId = row.paymentId,
                        customerId = row.customerId,
                        appointmentId = row.appointmentId ?: "",
                        deviceName = row.deviceName,
                        amount = row.amount,
                        date = row.date,
                        method = row.method,
                        status = row.status
                    )
                }
            }


            val (healedAppointments, healedPayments) =
                healDeviceNames(
                    loadedDevices = newDevices,

                    loadedAppointments =
                        newAppointments,

                    loadedPayments = newPayments
                )


            // =====================================================
            // Atomic swap: replace the snapshot lists with the
            // freshly fetched contents in one go. Compose's
            // snapshot system tracks state mutations on the
            // main thread, so this block must run there.
            //
            // We still bump dataVersion so the home / schedule
            // / payments viewmodels, which snapshot into
            // _uiState via _uiState.update on RefreshHome etc.,
            // get told to refresh.
            // =====================================================

            withContext(Dispatchers.Main) {

                devices.clear()
                newDevices.forEach { devices.add(it) }

                appointments.clear()
                healedAppointments.forEach { appointments.add(it) }

                payments.clear()
                healedPayments.forEach { payments.add(it) }

                bumpDataVersion()
                markColdLaunchLoaded()
            }


        } catch (t: Throwable) {

            Log.e("NekoEcoDB", "loadForCustomer failed", t)


            // =====================================================
            // On failure, leave the previous lists in place
            // (better UX than showing empty) but still bump
            // dataVersion so observers re-read and pick up
            // whatever is currently in the lists.
            // =====================================================

            bumpDataVersion()
        }
    }


    // ========================================================
    // LOAD ALL
    //
    // Populates the in-memory caches with EVERY customer /
    // appointment / payment row in the database. Used by the
    // staff session-restore path: the staff home reads from
    // CustomerDataStore.appointments (the single source of
    // truth in this codebase) so we need to make sure that
    // list is populated on staff login. loadForCustomer would
    // only fill the rows for one customer; loadAll fills
    // them all.
    //
    // Same suspend / atomic-swap contract as loadForCustomer
    // so the main thread isn't blocked.
    // ========================================================

    suspend fun loadAll() {

        val newDevices: List<CustomerDevice>
        val newAppointments: List<CustomerAppointment>
        val newPayments: List<CustomerPayment>


        try {

            newDevices = withContext(Dispatchers.IO) {

                DeviceRepo.instance.listAll().map { row ->

                    val accessories =
                        (row.accessories as? JsonArray)
                            ?.mapNotNull { element ->

                                val obj =
                                    element as? JsonObject
                                        ?: return@mapNotNull null

                                CustomerAccessory(
                                    name =
                                        obj["name"]
                                            ?.jsonPrimitive
                                            ?.content
                                            ?: "",
                                    price =
                                        obj["price"]
                                            ?.jsonPrimitive
                                            ?.double
                                            ?: 0.0,
                                    approved =
                                        obj["approved"]
                                            ?.jsonPrimitive
                                            ?.booleanOrNull
                                            ?: true
                                )
                            }
                            ?: emptyList()


                    CustomerDevice(
                        deviceId = row.deviceId,
                        customerId = row.customerId,
                        deviceName = row.deviceName,
                        category = row.category,
                        date = row.date,
                        status = row.status,
                        accessories = accessories,
                        recoveryValue = row.recoveryValue
                    )
                }
            }


            newAppointments = withContext(Dispatchers.IO) {

                AppointmentRepo.instance.listAll().map { row ->

                    CustomerAppointment(
                        appointmentId = row.appointmentId,
                        customerId = row.customerId,
                        deviceId = row.deviceId,
                        deviceName = row.deviceName,
                        date = row.date,
                        time = row.time,
                        company = row.company,
                        location = row.location,
                        status = row.status
                    )
                }
            }


            newPayments = withContext(Dispatchers.IO) {

                PaymentRepo.instance.listAll().map { row ->

                    CustomerPayment(
                        paymentId = row.paymentId,
                        customerId = row.customerId,
                        appointmentId = row.appointmentId ?: "",
                        deviceName = row.deviceName,
                        amount = row.amount,
                        date = row.date,
                        method = row.method,
                        status = row.status
                    )
                }
            }


            val (healedAppointments, healedPayments) =
                healDeviceNames(
                    loadedDevices = newDevices,

                    loadedAppointments =
                        newAppointments,

                    loadedPayments = newPayments
                )


            // Atomic swap on the main thread.
            withContext(Dispatchers.Main) {

                devices.clear()
                newDevices.forEach { devices.add(it) }

                appointments.clear()
                healedAppointments.forEach { appointments.add(it) }

                payments.clear()
                healedPayments.forEach { payments.add(it) }

                bumpDataVersion()

                // Flip the cold-launch flag on success too.
                // MainActivity's STAFF session restore uses
                // loadAll (not loadForCustomer), and
                // StaffMainScreen's dashboard spinner waits
                // on this flag — without this, a successful
                // staff cold launch sits on the spinner
                // forever.
                markColdLaunchLoaded()
            }


        } catch (t: Throwable) {

            Log.e("NekoEcoDB", "loadAll failed", t)


            // On failure, leave previous lists in place
            // and still bump dataVersion so observers
            // re-read whatever is currently there. Also
            // flip the cold-launch flag so the dashboard
            // doesn't stay on its spinner forever on a
            // network failure.
            bumpDataVersion()
            markColdLaunchLoaded()
        }
    }


    // ========================================================
    // UPDATE CUSTOMER NAME
    //
    // Used by the profile screen. Updates the in-memory
    // customer, writes through to Supabase (users table)
    // and keeps the persisted session name in sync so the
    // greeting is correct after a cold launch.
    // ========================================================

    fun updateCustomerName(
        newName: String
    ) {

        val cleanName =
            newName.trim()


        if (cleanName.isBlank()) {

            return
        }


        val customer =
            currentCustomer
                ?: return


        currentCustomer =
            customer.copy(
                name = cleanName
            )


        try {

            runBlocking {

                AuthRepo.instance.updateCustomerName(
                    customerId =
                        customer.customerId,

                    newName =
                        cleanName
                )
            }

        } catch (t: Throwable) {

            Log.e(
                "NekoEcoAuth",
                "updateCustomerName write-through failed",
                t
            )
        }


        SessionPreferences.persistCustomer(
            email =
                customer.email,

            name =
                cleanName,

            customerId =
                customer.customerId
        )


        bumpDataVersion()
    }


    // ========================================================
    // UPDATE CUSTOMER EMAIL
    //
    // Used by the profile screen. Writes through to
    // Supabase (users table) and keeps the persisted
    // session email in sync — the user then logs in with
    // the NEW address. On failure (duplicate email etc.)
    // the old address is kept.
    // ========================================================

    fun updateCustomerEmail(
        newEmail: String
    ) {

        val cleanEmail =
            newEmail.trim()


        if (!cleanEmail.contains("@")) {

            return
        }


        val customer =
            currentCustomer
                ?: return


        try {

            val updated =

                runBlocking {

                    AuthRepo.instance.updateCustomerEmail(
                        customerId =
                            customer.customerId,

                        newEmail =
                            cleanEmail
                    )
                }


            if (!updated) {

                return
            }


            currentCustomer =
                customer.copy(
                    email = cleanEmail
                )


            SessionPreferences.persistCustomer(
                email =
                    cleanEmail,

                name =
                    customer.name,

                customerId =
                    customer.customerId
            )


            bumpDataVersion()

        } catch (t: Throwable) {

            Log.e(
                "NekoEcoAuth",
                "updateCustomerEmail failed",
                t
            )
        }
    }


    // ========================================================
    // LOGOUT
    // ========================================================

    fun logout() {

        currentCustomer =
            null

        SessionPreferences.clear()
    }


    // ========================================================
    // CUSTOMER DEVICES
    //
    // Empty on startup. Populated by loadForCustomer() after
    // session restore. Any seed row here would (a) leak to the
    // staff home screen as a "Scheduled" pickup that no real
    // customer created, and (b) confuse the customer UI until
    // the loader replaces it. With Supabase wired up, the
    // loader is the single source of truth.
    // ========================================================

    val devices =
        mutableStateListOf<CustomerDevice>()


    // ========================================================
    // CUSTOMER APPOINTMENTS
    // ========================================================

    val appointments =
        mutableStateListOf<CustomerAppointment>()


    // ========================================================
    // CUSTOMER PAYMENTS
    // ========================================================

    val payments =
        mutableStateListOf<CustomerPayment>()


    // ========================================================
    // GET CURRENT CUSTOMER DEVICES
    // ========================================================

    fun getCurrentCustomerDevices():
            List<CustomerDevice> {

        val customerId =
            currentCustomer?.customerId
                ?: return emptyList()


        return devices.filter {

            it.customerId ==
                    customerId
        }
    }


    // ========================================================
    // GET CURRENT CUSTOMER APPOINTMENTS
    // ========================================================

    fun getCurrentCustomerAppointments():
            List<CustomerAppointment> {

        val customerId =
            currentCustomer?.customerId
                ?: return emptyList()


        return appointments.filter {

            it.customerId ==
                    customerId
        }
    }


    // ========================================================
    // GET CURRENT CUSTOMER PAYMENTS
    // ========================================================

    fun getCurrentCustomerPayments():
            List<CustomerPayment> {

        val customerId =
            currentCustomer?.customerId
                ?: return emptyList()


        return payments.filter {

            it.customerId ==
                    customerId
        }
    }


    // ========================================================
    // ADD DEVICE
    // ========================================================

    fun addDevice(
        device: CustomerDevice
    ) {

        val customer =
            currentCustomer
                ?: return


        val customerDevice =

            if (
                device.customerId ==
                customer.customerId
            ) {

                device

            } else {

                device.copy(
                    customerId =
                        customer.customerId
                )
            }


        if (
            devices.any {

                it.deviceId ==
                        customerDevice.deviceId
            }
        ) {

            return
        }


        devices.add(
            customerDevice
        )


        // ====================================================
        // SYNC STAFF
        // ====================================================

        StaffDataStore.addDeviceFromCustomer(

            device =
                customerDevice,

            customerName =
                customer.name
        )


        // ====================================================
        // WRITE-THROUGH TO SUPABASE
        // ====================================================

        persistDevice(customerDevice, customer.name)
    }


    // ========================================================
    // WRITE-THROUGH HELPERS
    // ========================================================

    private fun persistDevice(
        device: CustomerDevice,
        customerName: String
    ) {

        try {

            val accessoriesJson = JsonArray(
                device.accessories.map { acc ->
                    kotlinx.serialization.json.buildJsonObject {
                        put("name", kotlinx.serialization.json.JsonPrimitive(acc.name))
                        put("price", kotlinx.serialization.json.JsonPrimitive(acc.price))
                        put("approved", kotlinx.serialization.json.JsonPrimitive(acc.approved))
                    }
                }
            )

            runBlocking {

                DeviceRepo.instance.insertDevice(
                    NewDeviceRow(
                        deviceId      = device.deviceId,
                        customerId    = device.customerId,
                        customerName  = customerName,
                        deviceName    = device.deviceName,
                        category      = device.category,
                        date          = device.date,
                        status        = device.status,
                        accessories   = accessoriesJson,
                        recoveryValue = device.recoveryValue
                    )
                )
            }

        } catch (t: Throwable) {

            Log.e("NekoEcoDB", "persistDevice failed", t)
        }
    }


    private fun persistAppointment(
        appt: CustomerAppointment
    ) {

        try {

            runBlocking {

                AppointmentRepo.instance.insertAppointment(
                    NewAppointmentRow(
                        appointmentId = appt.appointmentId,
                        customerId    = appt.customerId,
                        deviceId      = appt.deviceId,
                        deviceName    = appt.deviceName,
                        date          = appt.date,
                        time          = appt.time,
                        company       = appt.company,
                        location      = appt.location,
                        status        = appt.status
                    )
                )
            }

        } catch (t: Throwable) {

            Log.e("NekoEcoDB", "persistAppointment failed", t)
        }
    }


    private fun persistPayment(
        payment: CustomerPayment
    ) {

        // ====================================================
        // The in-memory CustomerPayment doesn't carry
        // deviceId, but the DB schema requires it (NOT NULL).
        // Look it up from the linked appointment.
        // ====================================================

        val deviceId =
            appointments
                .find { it.appointmentId == payment.appointmentId }
                ?.deviceId
                ?: ""


        try {

            runBlocking {

                PaymentRepo.instance.insertPayment(
                    NewPaymentRow(
                        paymentId     = payment.paymentId,
                        customerId    = payment.customerId,
                        appointmentId = payment.appointmentId,
                        deviceId      = deviceId,
                        deviceName    = payment.deviceName,
                        amount        = payment.amount,
                        date          = payment.date,
                        method        = payment.method,
                        status        = payment.status
                    )
                )
            }

        } catch (t: Throwable) {

            Log.e("NekoEcoDB", "persistPayment failed", t)
        }
    }


    // ========================================================
    // DELETE DEVICE
    //
    // Keep this function for older parts of the app.
    // Customer deletion also removes the Staff copy.
    // ========================================================

    fun deleteDeviceByCustomer(
        deviceId: String
    ) {

        val customerId =
            currentCustomer?.customerId
                ?: return


        val device =
            devices.find {

                it.deviceId ==
                        deviceId &&

                        it.customerId ==
                        customerId

            } ?: return


        // ====================================================
        // DELETION RULES
        //
        // Pending and Rejected are always deletable.
        //
        // Approved is deletable only when every linked
        // appointment has been Cancelled — otherwise the
        // device is still in an active pickup flow.
        //
        // Scheduled / Completed are never deletable.
        // ====================================================

        val deletable =
            device.status == "Pending" ||
                    device.status == "Rejected" ||

                    (
                            device.status == "Approved" &&

                                    appointments
                                        .filter {
                                            it.deviceId == deviceId &&
                                                    it.customerId == customerId
                                        }
                                        .all { it.status == "Cancelled" }
                            )

        if (!deletable) {

            return
        }


        // ====================================================
        // GET APPOINTMENT IDS
        // ====================================================

        val removedAppointmentIds =

            appointments
                .filter {

                    it.deviceId ==
                            deviceId &&

                            it.customerId ==
                            customerId
                }
                .map {

                    it.appointmentId
                }


        // ====================================================
        // REMOVE CUSTOMER APPOINTMENTS
        // ====================================================

        appointments.removeAll {

            it.deviceId ==
                    deviceId &&

                    it.customerId ==
                    customerId
        }


        // ====================================================
        // REMOVE CUSTOMER PAYMENTS
        // ====================================================

        payments.removeAll {

            it.customerId ==
                    customerId &&

                    (
                            it.appointmentId in
                                    removedAppointmentIds ||

                                    it.deviceName ==
                                    device.deviceName
                            )
        }


        // ====================================================
        // REMOVE CUSTOMER DEVICE
        // ====================================================

        devices.removeAll {

            it.deviceId ==
                    deviceId &&

                    it.customerId ==
                    customerId
        }


        // ====================================================
        // REMOVE STAFF COPY
        // ====================================================

        StaffDataStore
            .deleteDeviceFromCustomer(
                deviceId
            )


        // ====================================================
        // DELETE FROM SUPABASE
        //
        // Order: appointments -> payments -> device, because
        // appointments.device_id is plain text (no FK), but
        // cleaning in this order keeps the DB tidy. If the
        // device delete fails we log it loudly so the bug is
        // visible in logcat; the local list has already been
        // cleared so the user still sees the row gone in
        // this session, and the next loadForCustomer will
        // re-sync from the DB (which is the truth).
        // ====================================================

        var serverDeleteSucceeded = false

        try {

            runBlocking {

                AppointmentRepo.instance
                    .deleteByDevice(deviceId)

                PaymentRepo.instance
                    .deleteByDevice(deviceId)


                serverDeleteSucceeded =
                    DeviceRepo.instance
                        .deleteDevice(deviceId)

                if (!serverDeleteSucceeded) {

                    Log.e(
                        "NekoEcoDB",
                        "deleteDeviceByCustomer: Supabase delete for $deviceId affected 0 rows"
                    )
                }
            }

        } catch (t: Throwable) {

            Log.e(
                "NekoEcoDB",
                "deleteDeviceByCustomer write-through failed",
                t
            )
        }


        // =====================================================
        // If the server-side delete failed, restore the device
        // locally so the UI matches the source of truth.
        // Otherwise the local list says "deleted" but
        // loadForCustomer pulls the row right back.
        // =====================================================

        if (!serverDeleteSucceeded) {

            val restored =
                device.copy(status = device.status)

            devices.add(restored)
        }
    }


    // ========================================================
    // DELETE DEVICE FROM STAFF
    //
    // This function can remain for compatibility.
    // Staff delete itself should NOT call this anymore.
    // ========================================================

    fun deleteDeviceFromStaff(
        deviceId: String
    ) {

        val device =
            devices.find {

                it.deviceId ==
                        deviceId

            } ?: return


        val customerId =
            device.customerId


        val removedAppointmentIds =

            appointments
                .filter {

                    it.deviceId ==
                            deviceId &&

                            it.customerId ==
                            customerId
                }
                .map {

                    it.appointmentId
                }


        appointments.removeAll {

            it.deviceId ==
                    deviceId &&

                    it.customerId ==
                    customerId
        }


        payments.removeAll {

            it.customerId ==
                    customerId &&

                    it.appointmentId in
                    removedAppointmentIds
        }


        devices.remove(
            device
        )


        // ====================================================
        // DELETE FROM SUPABASE
        //
        // Remove the device AND its appointment/payment rows
        // (both keyed by device_id). Without this the rows
        // reappear on the next loadForCustomer / loadAll.
        // ====================================================

        try {

            runBlocking {
                DeviceRepo.instance.deleteDevice(deviceId)
                AppointmentRepo.instance.deleteByDevice(deviceId)
                PaymentRepo.instance.deleteByDevice(deviceId)
            }

        } catch (t: Throwable) {

            Log.e(
                "NekoEcoDB",
                "deleteDeviceFromStaff write-through failed",
                t
            )
        }
    }


    // ========================================================
    // UPDATE DEVICE INSPECTION
    //
    // Used by StaffDataStore when the staff approves a
    // device after ticking which parts the company will
    // actually recover. Updates the in-memory customer
    // device (per-part approved flags + recomputed
    // recovery value) and persists both to Supabase.
    // Does NOT call StaffDataStore again.
    // ========================================================

    fun updateDeviceInspection(
        deviceId: String,
        approvedNames: Set<String>,
        newTotal: Double
    ) {

        val index =
            devices.indexOfFirst {

                it.deviceId ==
                        deviceId
            }


        if (
            index == -1
        ) {

            return
        }


        val updated =
            devices[index].copy(

                accessories =
                    devices[index].accessories.map { acc ->

                        acc.copy(
                            approved =
                                approvedNames.contains(
                                    acc.name
                                )
                        )
                    },

                recoveryValue =
                    newTotal
            )


        devices[index] = updated


        // Persist the approved parts + new total so the
        // inspection result survives reloads.

        try {

            val accessoriesJson = JsonArray(
                updated.accessories.map { acc ->
                    kotlinx.serialization.json.buildJsonObject {
                        put("name", kotlinx.serialization.json.JsonPrimitive(acc.name))
                        put("price", kotlinx.serialization.json.JsonPrimitive(acc.price))
                        put("approved", kotlinx.serialization.json.JsonPrimitive(acc.approved))
                    }
                }
            )

            runBlocking {

                DeviceRepo.instance.updateInspection(
                    deviceId = deviceId,
                    accessories = accessoriesJson,
                    recoveryValue = newTotal
                )
            }

        } catch (t: Throwable) {

            Log.e("NekoEcoDB", "updateInspection persist skipped", t)
        }


        bumpDataVersion()
    }


    // ========================================================
    // UPDATE DEVICE STATUS
    //
    // Used mainly by StaffDataStore.
    //
    // Does NOT call StaffDataStore again.
    // ========================================================

    fun updateDeviceStatus(
        deviceId: String,
        newStatus: String
    ) {

        val index =
            devices.indexOfFirst {

                it.deviceId ==
                        deviceId
            }


        if (
            index == -1
        ) {

            return
        }


        val customerId =
            devices[index]
                .customerId


        // ====================================================
        // UPDATE CUSTOMER DEVICE
        // ====================================================

        devices[index] =

            devices[index]
                .copy(
                    status =
                        newStatus
                )


        // ====================================================
        // UPDATE CUSTOMER APPOINTMENTS
        // ====================================================

        val appointmentIndexes =

            appointments
                .mapIndexedNotNull {
                        appointmentIndex,
                        appointment ->

                    if (
                        appointment.deviceId ==
                        deviceId &&

                        appointment.customerId ==
                        customerId
                    ) {

                        appointmentIndex

                    } else {

                        null
                    }
                }


        appointmentIndexes
            .forEach { appointmentIndex ->

                val appointment =
                    appointments[appointmentIndex]


                when (
                    newStatus
                ) {

                    "Scheduled" -> {

                        appointments[appointmentIndex] =

                            appointment.copy(
                                status =
                                    "Scheduled"
                            )
                    }


                    "Completed" -> {

                        appointments[appointmentIndex] =

                            appointment.copy(
                                status =
                                    "Completed"
                            )
                    }


                    "Rejected" -> {

                        appointments[appointmentIndex] =

                            appointment.copy(
                                status =
                                    "Cancelled"
                            )
                    }
                }
            }


        // ====================================================
        // WRITE-THROUGH TO SUPABASE
        // ====================================================

        try {

            runBlocking {
                DeviceRepo.instance.updateStatus(deviceId, newStatus)
            }
        } catch (t: Throwable) {
            Log.e("NekoEcoDB", "updateDeviceStatus write-through failed", t)
        }
    }


    // ========================================================
    // UPDATE DEVICE NAME
    // ========================================================

    fun updateDeviceName(
        deviceId: String,
        newName: String
    ) {

        val cleanName =
            newName.trim()


        if (
            cleanName.isBlank()
        ) {

            return
        }


        val deviceIndex =
            devices.indexOfFirst {

                it.deviceId ==
                        deviceId
            }


        if (
            deviceIndex == -1
        ) {

            return
        }


        val oldDevice =
            devices[deviceIndex]


        // ====================================================
        // COMPLETED DEVICE CANNOT BE EDITED
        // ====================================================

        if (
            oldDevice.status ==
            "Completed"
        ) {

            return
        }


        // ====================================================
        // UPDATE CUSTOMER DEVICE
        // ====================================================

        devices[deviceIndex] =

            oldDevice.copy(
                deviceName =
                    cleanName
            )


        // ====================================================
        // UPDATE CUSTOMER APPOINTMENTS
        // ====================================================

        appointments
            .mapIndexedNotNull {
                    index,
                    appointment ->

                if (
                    appointment.deviceId ==
                    deviceId
                ) {

                    index

                } else {

                    null
                }
            }
            .forEach { index ->

                appointments[index] =

                    appointments[index]
                        .copy(
                            deviceName =
                                cleanName
                        )
            }


        // ====================================================
        // UPDATE CUSTOMER PAYMENTS
        // ====================================================

        payments
            .mapIndexedNotNull {
                    index,
                    payment ->

                if (
                    payment.customerId ==
                    oldDevice.customerId &&

                    payment.deviceName ==
                    oldDevice.deviceName
                ) {

                    index

                } else {

                    null
                }
            }
            .forEach { index ->

                payments[index] =

                    payments[index]
                        .copy(
                            deviceName =
                                cleanName
                        )
            }


        // ====================================================
        // SYNC STAFF
        // ====================================================

        StaffDataStore
            .updateDeviceNameFromCustomer(

                deviceId =
                    deviceId,

                newName =
                    cleanName
            )


        // ====================================================
        // WRITE-THROUGH TO SUPABASE
        //
        // Without this the change is lost on the next
        // loadForCustomer — the in-memory list and the staff
        // mirror update fine, but the server still has the
        // old device_name and a refresh pulls the stale value
        // back.
        // ====================================================

        try {

            runBlocking {

                val updated =
                    DeviceRepo.instance
                        .updateName(
                            deviceId = deviceId,
                            newName = cleanName
                        )

                if (!updated) {

                    Log.e(
                        "NekoEcoDB",
                        "updateDeviceName: Supabase update for $deviceId matched 0 rows"
                    )
                }


                // Appointments and payments store a COPIED
                // device_name — update their rows too, or a
                // reload pulls the stale name back into the
                // Schedule tab and home's upcoming pickups.

                AppointmentRepo.instance.updateDeviceName(
                    deviceId = deviceId,
                    newName = cleanName
                )

                PaymentRepo.instance.updateDeviceName(
                    deviceId = deviceId,
                    newName = cleanName
                )
            }

        } catch (t: Throwable) {

            Log.e(
                "NekoEcoDB",
                "updateDeviceName write-through failed",
                t
            )
        }


        // Re-render Schedule / Home, which refresh on
        // dataVersion.

        bumpDataVersion()
    }


    // ========================================================
    // ADD APPOINTMENT
    // ========================================================
    fun addAppointment(
        appointment: CustomerAppointment
    ) {

        val customer =
            currentCustomer
                ?: return


        val customerAppointment =

            if (
                appointment.customerId ==
                customer.customerId
            ) {

                appointment

            } else {

                appointment.copy(
                    customerId =
                        customer.customerId
                )
            }


        // Prevent duplicate
        if (
            appointments.any {

                it.appointmentId ==
                        customerAppointment
                            .appointmentId
            }
        ) {

            return
        }

        appointments.add(
            customerAppointment
        )

        StaffDataStore.addAppointmentFromCustomer(
            customerAppointment
        )


        // ====================================================
        // WRITE-THROUGH TO SUPABASE
        // ====================================================

        persistAppointment(customerAppointment)
    }

    // ========================================================
    // ADD APPOINTMENT FROM STAFF
    // ========================================================

    fun addAppointmentFromStaff(
        appointment: CustomerAppointment
    ) {

        if (
            appointments.any {

                it.appointmentId ==
                        appointment.appointmentId
            }
        ) {

            return
        }


        appointments.add(
            appointment
        )
    }


    // ========================================================
    // UPDATE APPOINTMENT STATUS
    // ========================================================

    fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: String
    ) {

        val customerId =
            currentCustomer?.customerId
                ?: return


        val index =
            appointments.indexOfFirst {

                it.appointmentId ==
                        appointmentId &&

                        it.customerId ==
                        customerId
            }


        if (
            index == -1
        ) {

            return
        }


        appointments[index] =

            appointments[index]
                .copy(
                    status =
                        newStatus
                )


        // ====================================================
        // SYNC DEVICE STATUS
        // ====================================================

        val deviceId =
            appointments[index]
                .deviceId


        val deviceIndex =
            devices.indexOfFirst {

                it.deviceId ==
                        deviceId &&

                        it.customerId ==
                        customerId
            }


        if (
            deviceIndex != -1
        ) {

            when (
                newStatus
            ) {

                // NOTE: a "Scheduled" appointment no longer
                // overwrites the device status — the device
                // lifecycle (Pending / Approved / Rejected /
                // Completed) is independent of the pickup
                // state, which lives on the appointment.

                "Completed" -> {

                    devices[deviceIndex] =

                        devices[deviceIndex]
                            .copy(
                                status =
                                    "Completed"
                            )
                }


                "Cancelled" -> {

                    // Customer cancellation does not
                    // reject the device.

                    if (
                        devices[deviceIndex]
                            .status !=
                        "Rejected"
                    ) {

                        devices[deviceIndex] =

                            devices[deviceIndex]
                                .copy(
                                    status =
                                        "Approved"
                                )
                    }
                }
            }
        }


        // ====================================================
        // WRITE-THROUGH APPOINTMENT STATUS TO SUPABASE
        // ====================================================

        try {

            runBlocking {
                AppointmentRepo.instance.updateStatus(appointmentId, newStatus)
            }
        } catch (t: Throwable) {
            Log.e("NekoEcoDB", "updateAppointmentStatus write-through failed", t)
        }
    }


    // ========================================================
    // UPDATE APPOINTMENT FROM STAFF
    // ========================================================

    fun updateAppointmentFromStaff(
        appointmentId: String,
        newDate: String,
        newTime: String,
        newStatus: String
    ) {

        val index =
            appointments.indexOfFirst {

                it.appointmentId ==
                        appointmentId
            }


        if (
            index == -1
        ) {

            return
        }


        appointments[index] =

            appointments[index]
                .copy(

                    date =
                        newDate,

                    time =
                        newTime,

                    status =
                        newStatus
                )


        // ====================================================
        // WRITE-THROUGH APPOINTMENT TO SUPABASE
        //
        // Date/time first (that's what a reschedule
        // changes), then status — each guarded separately
        // so one failure can't silently skip the other.
        // ====================================================

        try {

            runBlocking {

                val dateOk =
                    AppointmentRepo.instance.updateDateTime(
                        appointmentId = appointmentId,
                        newDate = newDate,
                        newTime = newTime
                    )

                val statusOk =
                    AppointmentRepo.instance.updateStatus(
                        appointmentId = appointmentId,
                        newStatus = newStatus
                    )

                Log.d(
                    "NekoEcoDB",
                    "updateAppointmentFromStaff: dateOk=$dateOk statusOk=$statusOk"
                )
            }
        } catch (t: Throwable) {
            Log.e("NekoEcoDB", "updateAppointmentFromStaff write-through failed", t)
        }
    }


    // ========================================================
    // CANCEL APPOINTMENT
    // ========================================================

    fun cancelAppointment(
        appointmentId: String
    ) {

        val customerId =
            currentCustomer?.customerId
                ?: return


        val appointment =
            appointments.find {

                it.appointmentId ==
                        appointmentId &&

                        it.customerId ==
                        customerId

            } ?: return


        // Pending (awaiting staff inspection approval)
        // and Scheduled can be cancelled
        if (
            appointment.status !=
            "Scheduled" &&

            appointment.status !=
            "Pending"
        ) {

            return
        }


        updateAppointmentStatus(

            appointmentId =
                appointmentId,

            newStatus =
                "Cancelled"
        )


        // ====================================================
        // SYNC STAFF
        // ====================================================

        StaffDataStore
            .cancelAppointmentByCustomer(

                appointmentId =
                    appointmentId
            )
    }


    // ========================================================
    // GENERATE APPOINTMENT ID
    // ========================================================

    fun generateAppointmentId():
            String {

        val maxNumber =
            appointments
                .mapNotNull {

                    it.appointmentId
                        .removePrefix("APP")
                        .toIntOrNull()
                }
                .maxOrNull()
                ?: 0


        return "APP" +
                (maxNumber + 1)
                    .toString()
                    .padStart(3, '0')
    }


    // ========================================================
    // ADD PAYMENT FROM STAFF
    //
    // Used by StaffDataStore when the approval chain
    // creates a payment for an approved device. Dedupes
    // by paymentId, then persists with the deviceId
    // back-filled from the linked appointment.
    // ========================================================

    fun addPaymentFromStaff(
        payment: CustomerPayment
    ) {

        if (
            payments.any {

                it.paymentId ==
                        payment.paymentId
            }
        ) {

            return
        }


        payments.add(
            payment
        )


        // ====================================================
        // WRITE-THROUGH TO SUPABASE
        // ====================================================

        persistPayment(payment)
    }


    // ========================================================
    // UPDATE PAYMENT STATUS FROM STAFF
    // ========================================================

    fun updatePaymentStatusFromStaff(
        paymentId: String,
        newStatus: String,
        // Optional new payment method (Bank Transfer /
        // Touch 'n Go / Cash) set during staff payment
        // collection. When null the method is unchanged.
        newMethod: String? = null,
        // Optional new payout amount — the staff
        // inspection can change the approved total after
        // the payment row exists. When null the amount
        // is unchanged.
        newAmount: Double? = null
    ) {

        val index =
            payments.indexOfFirst {

                it.paymentId ==
                        paymentId
            }


        if (
            index == -1
        ) {

            return
        }


        payments[index] =

            payments[index]
                .copy(
                    status =
                        newStatus,

                    method =

                        newMethod
                            ?: payments[index]
                                .method,

                    amount =

                        newAmount
                            ?: payments[index]
                                .amount
                )


        // ====================================================
        // WRITE-THROUGH PAYMENT STATUS TO SUPABASE
        // ====================================================

        try {

            runBlocking {
                PaymentRepo.instance.updateStatus(
                    paymentId = paymentId,
                    newStatus = newStatus,
                    newMethod = newMethod,
                    newAmount = newAmount
                )
            }
        } catch (t: Throwable) {
            Log.e("NekoEcoDB", "updatePaymentStatusFromStaff write-through failed", t)
        }
    }

    // ========================================================
    // GENERATE DEVICE ID
    // ========================================================

    fun generateDeviceId():
            String {

        val customerIds =

            devices.mapNotNull {

                it.deviceId
                    .removePrefix("DEV")
                    .toIntOrNull()
            }


        val staffIds =

            StaffDataStore
                .devices
                .mapNotNull {

                    it.id
                        .removePrefix("DEV")
                        .toIntOrNull()
                }


        val maxNumber =

            (customerIds + staffIds)
                .maxOrNull()
                ?: 0


        return "DEV" +
                (maxNumber + 1)
                    .toString()
                    .padStart(3, '0')
    }



    // ========================================================
    // DEVICES SOLD
    // ========================================================

    fun getDevicesSold():
            Int {

        return getCurrentCustomerDevices()
            .count {

                it.status ==
                        "Completed"
            }
    }


    // ========================================================
    // CO2 SAVED
    // ========================================================

    fun getCO2Saved():
            Double {

        return getCurrentCustomerDevices()
            .sumOf {

                it.recoveryValue
            } * 0.35
    }
}