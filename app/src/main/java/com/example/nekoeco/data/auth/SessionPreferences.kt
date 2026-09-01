package com.example.nekoeco.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


// ============================================================
// SESSION PREFERENCES
//
// Persists the most recent successful login so that a fresh
// app launch can skip past the role-selection screen and land
// the user directly on the right home screen.
//
// Storage shape (in the existing `neko_eco_prefs` file, so
// no new dependencies):
//   - session_role : "customer" | "staff" | absent
//   - session_id   : customer email or staff id (lookup key)
//   - session_name : display name (best-effort, for the splash)
//
// We never persist the password. On launch we re-validate
// the persisted identity against Supabase; if the account is
// gone (deleted, etc.) we clear the session and fall back to
// the login screen.
//
// Pattern mirrors ThemePreferences for consistency.
// ============================================================


sealed interface SessionState {

    data object Loading : SessionState


    data object LoggedOut : SessionState


    data class LoggedInCustomer(
        val email: String,

        // The user's UUID in the users table. Cached at
        // login so cold launches can skip the
        // findCustomerByEmail round-trip and go straight
        // to loadForCustomer(storedId). Falls back to null
        // for sessions persisted before this field existed
        // — those launches still re-validate.
        val customerId: String? = null,

        // Display name persisted at login so the home
        // greeting shows the real name instead of the
        // email. null for legacy sessions — the restore
        // path backfills it once.
        val name: String? = null
    ) : SessionState


    data class LoggedInStaff(
        val staffId: String,

        // The user's UUID in the users table (same purpose
        // as above for staff sessions).
        val recordId: String? = null
    ) : SessionState
}


object SessionPreferences {

    private const val PREFS_NAME = "neko_eco_prefs"
    private const val KEY_ROLE = "session_role"
    private const val KEY_ID = "session_id"
    private const val KEY_NAME = "session_name"
    private const val KEY_CUSTOMER_ID = "session_customer_id"
    private const val KEY_STAFF_RECORD_ID = "session_staff_record_id"

    private const val ROLE_CUSTOMER = "customer"
    private const val ROLE_STAFF = "staff"

    private lateinit var prefs: SharedPreferences

    private val _session = MutableStateFlow<SessionState>(
        SessionState.Loading
    )


    val session: StateFlow<SessionState> =
        _session.asStateFlow()


    fun init(context: Context) {

        if (::prefs.isInitialized) return

        prefs = context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )


        _session.value = readPersistedSession()
    }


    private fun readPersistedSession():
            SessionState {

        if (!::prefs.isInitialized) {
            return SessionState.LoggedOut
        }


        val role =
            prefs.getString(KEY_ROLE, null)

        val id =
            prefs.getString(KEY_ID, null)


        if (role.isNullOrBlank() ||
            id.isNullOrBlank()
        ) {
            return SessionState.LoggedOut
        }


        return when (role) {

            ROLE_CUSTOMER ->
                SessionState.LoggedInCustomer(

                    email = id,

                    // Cached at login so warm launches can
                    // skip the findCustomerByEmail round-trip
                    // and call loadForCustomer(storedId)
                    // directly. null for sessions persisted
                    // before this key existed — those
                    // launches still re-validate.
                    customerId =
                        prefs.getString(
                            KEY_CUSTOMER_ID, null
                        )
                )

            ROLE_STAFF ->
                SessionState.LoggedInStaff(

                    staffId = id,

                    recordId =
                        prefs.getString(
                            KEY_STAFF_RECORD_ID, null
                        )
                )

            else ->
                SessionState.LoggedOut
        }
    }


    fun persistCustomer(
        email: String,
        name: String? = null,
        customerId: String? = null
    ) {

        if (!::prefs.isInitialized) return

        prefs.edit()
            .putString(KEY_ROLE, ROLE_CUSTOMER)
            .putString(KEY_ID, email)
            .apply()

        if (!name.isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_NAME, name)
                .apply()
        }

        if (!customerId.isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_CUSTOMER_ID, customerId)
                .apply()
        }


        _session.value =
            SessionState.LoggedInCustomer(
                email = email,
                customerId = customerId,
                name = name
            )
    }


    fun persistStaff(
        staffId: String,
        name: String? = null,
        recordId: String? = null
    ) {

        if (!::prefs.isInitialized) return

        prefs.edit()
            .putString(KEY_ROLE, ROLE_STAFF)
            .putString(KEY_ID, staffId)
            .apply()

        if (!name.isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_NAME, name)
                .apply()
        }

        if (!recordId.isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_STAFF_RECORD_ID, recordId)
                .apply()
        }


        _session.value =
            SessionState.LoggedInStaff(
                staffId = staffId,
                recordId = recordId
            )
    }


    fun clear() {

        if (::prefs.isInitialized) {

            prefs.edit()
                .remove(KEY_ROLE)
                .remove(KEY_ID)
                .remove(KEY_NAME)
                .remove(KEY_CUSTOMER_ID)
                .remove(KEY_STAFF_RECORD_ID)
                .apply()
        }


        _session.value = SessionState.LoggedOut
    }
}