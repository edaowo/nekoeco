package com.example.nekoeco.data.auth

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns

class AuthRepository(
    private val postgrest: Postgrest =
        SupabaseClientProvider.client.pluginManager.getPlugin(Postgrest)
) {


    // ========================================================
// Existence check — used before insert to surface a real
// "email already registered" answer rather than masking
// unrelated network/parse errors as duplicate-email.
//
// We decode the count check as a small IdOnlyRow data class
// instead of UserRow, because we only SELECT the `id`
// column and UserRow requires role/name/password_hash.
// ========================================================

    suspend fun emailExists(email: String): Boolean {

        val rows = postgrest
            .from("users")
            .select(
                columns = Columns.list("id")
            ) {
                filter {
                    eq("role", "customer")
                    eq("email", email)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()

        return rows.isNotEmpty()
    }


    suspend fun findCustomerByEmailAndHash(
        email: String,
        hash: String
    ): UserRow? {

        val rows = postgrest
            .from("users")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("role", "customer")
                    eq("email", email)
                    eq("password_hash", hash)
                }
                limit(1)
            }
            .decodeList<UserRow>()

        return rows.firstOrNull()
    }


    suspend fun findCustomerByEmailAndName(
        email: String,
        name: String
    ): Boolean {

        val rows = postgrest
            .from("users")
            .select(
                columns = Columns.list("id")
            ) {
                filter {
                    eq("role", "customer")
                    eq("email", email)
                    eq("name", name)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()

        return rows.isNotEmpty()
    }


    suspend fun findStaffByIdAndHash(
        staffId: String,
        hash: String
    ): UserRow? {

        val rows = postgrest
            .from("users")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("role", "staff")
                    eq("staff_id", staffId)
                    eq("password_hash", hash)
                }
                limit(1)
            }
            .decodeList<UserRow>()

        return rows.firstOrNull()
    }


    suspend fun findStaffById(
        staffId: String
    ): UserRow? {

        val rows = postgrest
            .from("users")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("role", "staff")
                    eq("staff_id", staffId)
                }
                limit(1)
            }
            .decodeList<UserRow>()

        return rows.firstOrNull()
    }


    // ========================================================
    // Look up a customer by email alone. Used by the
    // session-restore path on app launch: we persisted the
    // email at login, but never the password, so we can only
    // confirm "this account still exists". If it does, we
    // re-seed the in-memory currentCustomer; if not, we clear
    // the persisted session and fall back to the login screen.
    // ========================================================

    suspend fun findCustomerByEmail(
        email: String
    ): UserRow? {

        val rows = postgrest
            .from("users")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("role", "customer")
                    eq("email", email)
                }
                limit(1)
            }
            .decodeList<UserRow>()

        return rows.firstOrNull()
    }


    // ========================================================
    // UPDATE NAME — profile screen rename. Filtered by the
    // user's UUID (role-agnostic is fine here because the
    // id is unique).
    // ========================================================

    suspend fun updateCustomerName(
        customerId: String,
        newName: String
    ): Boolean {

        return try {

            postgrest
                .from("users")
                .update(
                    {
                        set("name", newName)
                    }
                ) {
                    filter {
                        eq("id", customerId)
                    }
                }

            true

        } catch (t: Throwable) {

            false
        }
    }


    // ========================================================
    // UPDATE EMAIL — profile screen email change. The
    // users table enforces email uniqueness; a duplicate
    // (409) or any other failure returns false so the
    // caller keeps the old address.
    // ========================================================

    suspend fun updateCustomerEmail(
        customerId: String,
        newEmail: String
    ): Boolean {

        return try {

            postgrest
                .from("users")
                .update(
                    {
                        set("email", newEmail)
                    }
                ) {
                    filter {
                        eq("id", customerId)
                    }
                }

            true

        } catch (t: Throwable) {

            false
        }
    }


    // ========================================================
    // UPDATE STAFF NAME — staff profile screen rename.
    // recordId is the staff row's UUID in the users table.
    // ========================================================

    suspend fun updateStaffName(
        recordId: String,
        newName: String
    ): Boolean {

        return try {

            postgrest
                .from("users")
                .update(
                    {
                        set("name", newName)
                    }
                ) {
                    filter {
                        eq("id", recordId)
                    }
                }

            true

        } catch (t: Throwable) {

            false
        }
    }


    // ========================================================
    // Insert customer. We use NewUserRow (no `id` field) so
    // Postgres applies the gen_random_uuid() default.
    //
    // We deliberately do NOT decode the insert response here.
    // The Supabase Kotlin SDK's insert() returns an empty
    // body by default, and the SDK's internal Prefer header
    // builder doesn't always combine cleanly with our
    // append("Prefer", "return=representation"), causing
    // decodeSingle() to fail with "Expected start of the
    // array '['".
    //
    // Instead, we just fire-and-check (any throw = insert
    // failed, duplicate email = PostgRestException 409). The
    // caller (signUp in CustomerDataStore) handles the
    // returned row by re-fetching with findCustomerByEmail.
    //
    // To know whether the INSERT actually inserted (vs was
    // rejected as duplicate), we let the exception bubble
    // up: a 409 from PostgREST means "email already exists"
    // and the caller will see it and treat as duplicate.
    // ========================================================

    suspend fun insertCustomer(
        name: String,
        email: String,
        hash: String
    ): UserRow {

        val row = NewUserRow(
            role         = "customer",
            email        = email,
            staffId      = null,
            name         = name,
            passwordHash = hash
        )

        // Fire the INSERT. We don't read the body because it
        // is empty without Prefer: return=representation and
        // the SDK header-append path doesn't reliably set it.
        postgrest
            .from("users")
            .insert(row)


        // Re-fetch the row by email. There's a tiny race
        // window if another insert lands at the same email
        // between these two calls, but the email UNIQUE
        // constraint would have rejected our INSERT above,
        // so this lookup is safe.
        val fetched = postgrest
            .from("users")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("role", "customer")
                    eq("email", email)
                }
                limit(1)
            }
            .decodeList<UserRow>()

        return fetched.firstOrNull()
            ?: throw IllegalStateException(
                "Insert reported success but row not found for $email"
            )
    }


    suspend fun updateCustomerPassword(
        email: String,
        name: String,
        hash: String
    ): Boolean {

        // ================================================
        // UPDATE filtered by (role=customer, email, name)
        // so only the owner of the matching email+name can
        // reset. Any failure (404/406 on no-match) is
        // treated as "not updated" so the caller can show
        // the existing "Email or Name is incorrect." UI text.
        // ================================================

        runCatching {

            postgrest
                .from("users")
                .update(
                    {
                        set("password_hash", hash)
                    }
                ) {
                    filter {
                        eq("role", "customer")
                        eq("email", email)
                        eq("name", name)
                    }
                }

        }.onFailure {

            return false
        }


        // ================================================
        // PostgrestResult doesn't expose rowsAffected by
        // default. Re-query with the same filter to confirm
        // a row actually matched.
        // ================================================

        val matched = postgrest
            .from("users")
            .select(
                columns = Columns.list("id")
            ) {
                filter {
                    eq("role", "customer")
                    eq("email", email)
                    eq("name", name)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()


        return matched.isNotEmpty()
    }


    suspend fun updateStaffPassword(
        staffId: String,
        name: String,
        hash: String
    ): Boolean {

        runCatching {

            postgrest
                .from("users")
                .update(
                    {
                        set("password_hash", hash)
                    }
                ) {
                    filter {
                        eq("role", "staff")
                        eq("staff_id", staffId)
                        eq("name", name)
                    }
                }

        }.onFailure {

            // ================================================
            // UPDATE can throw on 404 / 406 if no row matched.
            // Treat any failure as "not updated" so the caller
            // shows the existing "Staff ID or Staff Name is
            // incorrect." message.
            // ================================================

            return false
        }


        // ====================================================
        // PostgrestResult doesn't expose rowsAffected by default,
        // so re-query with the same filter to confirm a row
        // actually matched. Acceptable extra round-trip for
        // the forgot-password recovery path.
        // ====================================================

        val matched = postgrest
            .from("users")
            .select(
                columns = Columns.list("id")
            ) {
                filter {
                    eq("role", "staff")
                    eq("staff_id", staffId)
                    eq("name", name)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()


        return matched.isNotEmpty()
    }
}


// ============================================================
// Small row shape used for "does a row matching these filters
// exist?" checks. We only SELECT the `id` column, so we
// can't decode as UserRow (which requires role/name/password_hash).
// ============================================================

@kotlinx.serialization.Serializable
data class IdOnlyRow(
    val id: String
)


object AuthRepo {

    val instance: AuthRepository =
        AuthRepository()
}