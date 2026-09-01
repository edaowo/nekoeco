package com.example.nekoeco

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.nekoeco.data.auth.AuthRepo
import com.example.nekoeco.data.auth.SessionPreferences
import com.example.nekoeco.data.auth.SessionState

import com.example.nekoeco.ui.customer.CustomerDataStore
import com.example.nekoeco.ui.customer.CustomerLoginScreen
import com.example.nekoeco.ui.customer.CustomerMainScreen
import com.example.nekoeco.ui.customer.CustomerSignUpScreen
import com.example.nekoeco.ui.customer.Customer

import com.example.nekoeco.ui.role.RoleSelectionScreen

import com.example.nekoeco.ui.staff.StaffAccount
import com.example.nekoeco.ui.staff.StaffAccounts
import com.example.nekoeco.ui.staff.StaffDataStore
import com.example.nekoeco.ui.staff.StaffLoginScreen
import com.example.nekoeco.ui.staff.StaffMainScreen

import com.example.nekoeco.ui.theme.NekoEcoTheme
import com.example.nekoeco.ui.theme.ThemePreferences

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        enableEdgeToEdge()

        ThemePreferences.init(this)

        SessionPreferences.init(this)


        setContent {

            val darkMode by ThemePreferences.darkMode.collectAsState()


            NekoEcoTheme(
                darkTheme =
                    darkMode
            ) {

                Surface(
                    modifier =
                        Modifier.fillMaxSize(),

                    color =
                        MaterialTheme
                            .colorScheme
                            .background
                ) {

                    AppRoot(
                        darkMode =
                            darkMode,

                        onDarkModeChange = {
                            ThemePreferences
                                .setDarkMode(it)
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// APP ROOT
//
// Collects the persisted session, decides which screen the
// NavHost should mount as the start destination, and renders
// a tiny splash while we re-validate the saved identity
// against Supabase.
// ============================================================

@Composable
private fun AppRoot(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {

    val sessionState by
        SessionPreferences
            .session
            .collectAsState()


    // ========================================================
    // SESSION RESTORE
    //
    // When the persisted state is a logged-in customer or
    // staff, we re-validate against Supabase on a background
    // coroutine. On success we re-seed the in-memory
    // currentCustomer / currentStaff so the home screen has
    // what it needs. On failure (account deleted, network
    // down + cache stale, etc.) we clear the persisted
    // session so the user lands on role_selection next.
    //
    // We default to the optimistic "logged in" decision for
    // the start destination: the user goes straight to home,
    // and if the re-validation fails we fall back to login
    // via the SessionState update (which retriggers the
    // NavHost startDestination).
    // ========================================================

    LaunchedEffect(sessionState) {

        when (val s = sessionState) {

            is SessionState.LoggedInCustomer -> {

                launch {

                    // ============================================
                    // FAST PATH (warm launch with cached UUID):
                    //
                    // We persisted the customer's UUID at login
                    // so we can skip the findCustomerByEmail
                    // round-trip on subsequent launches and go
                    // straight to loadForCustomer(storedId).
                    //
                    // SLOW PATH (first launch after this code
                    // shipped, or a session persisted before
                    // the UUID cache existed):
                    //
                    // Fall back to findCustomerByEmail to
                    // recover the UUID, persist it for next
                    // time, then load.
                    // ============================================

                    val cachedId = s.customerId


                    val customer: Customer? =

                        if (!cachedId.isNullOrBlank()) {

                            // Warm path: build the customer from
                            // the cached UUID. Prefer the display
                            // name persisted at login so the home
                            // greeting shows the real name, not
                            // the email. Legacy sessions without
                            // a stored name fetch it once via
                            // findCustomerByEmail and re-persist
                            // it for next launch.

                            if (!s.name.isNullOrBlank()) {

                                Customer(
                                    customerId = cachedId,
                                    name = s.name,
                                    email = s.email,
                                    password = ""
                                )

                            } else {

                                val row =
                                    withContext(Dispatchers.IO) {

                                        try {

                                            AuthRepo.instance
                                                .findCustomerByEmail(
                                                    s.email
                                                )

                                        } catch (t: Throwable) {

                                            null
                                        }
                                    }


                                if (row == null) {

                                    Customer(
                                        customerId = cachedId,
                                        name = s.email,
                                        email = s.email,
                                        password = ""
                                    )

                                } else {

                                    // Backfill the name so the
                                    // next launch skips this
                                    // fetch.

                                    SessionPreferences.persistCustomer(
                                        email = s.email,
                                        name = row.name,
                                        customerId = cachedId
                                    )


                                    Customer(
                                        customerId = cachedId,
                                        name = row.name,
                                        email = row.email
                                            ?: s.email,
                                        password = ""
                                    )
                                }
                            }

                        } else {

                            val row =
                                withContext(Dispatchers.IO) {

                                    try {

                                        AuthRepo.instance
                                            .findCustomerByEmail(
                                                s.email
                                            )

                                    } catch (t: Throwable) {

                                        null
                                    }
                                }


                            if (row == null) {

                                SessionPreferences.clear()
                                null

                            } else {

                                // Persist the UUID so the next
                                // launch can take the fast path.
                                SessionPreferences.persistCustomer(
                                    email = s.email,
                                    customerId = row.id
                                )


                                Customer(
                                    customerId = row.id,
                                    name = row.name,
                                    email = row.email
                                        ?: s.email,
                                    password = ""
                                )
                            }
                        }


                    if (customer != null) {

                        // ============================================
                        // Single source of truth for "load the
                        // customer's data on cold launch":
                        //
                        // Set currentCustomer AND trigger the
                        // load here, on the same background
                        // coroutine that validated the
                        // session. This guarantees the data
                        // is in flight (and usually already
                        // populated) by the time the
                        // CustomerMainScreen mounts, so the
                        // dashboard renders correctly on first
                        // paint without needing a tab switch.
                        // ============================================

                        CustomerDataStore
                            .currentCustomer = customer


                        CustomerDataStore
                            .loadForCustomer(
                                customer.customerId
                            )
                    }
                }
            }


            is SessionState.LoggedInStaff -> {

                launch {

                    // ============================================
                    // FAST PATH (warm launch with cached UUID):
                    // skip the findStaff round-trip.
                    //
                    // SLOW PATH: fall back to findStaff, then
                    // persist the UUID for next time.
                    // ============================================

                    val cachedId = s.recordId


                    val account: StaffAccount? = run {

                        if (!cachedId.isNullOrBlank()) {

                            return@run StaffAccounts.findStaffByIdFallback(
                                cachedId = cachedId,
                                staffId = s.staffId
                            )
                        }


                        val fetched =
                            withContext(Dispatchers.IO) {

                                try {

                                    StaffAccounts.findStaff(
                                        s.staffId
                                    )

                                } catch (t: Throwable) {

                                    null
                                }
                            }


                        if (fetched == null) {

                            SessionPreferences.clear()
                            return@run null
                        }


                        // Persist the record UUID for
                        // next launch.
                        SessionPreferences.persistStaff(
                            staffId = s.staffId,
                            recordId = fetched.recordId
                        )


                        fetched
                    }


                    if (account != null) {

                        StaffAccounts.currentStaff = account


                        // ============================================
                        // Staff home reads appointments / devices
                        // / payments from CustomerDataStore
                        // (the single source of truth in this
                        // codebase). Pull every customer's rows
                        // into the in-memory caches so the
                        // staff home "upcoming pickup" view
                        // shows real appointments immediately
                        // on cold start — without this, the
                        // lists are empty after process restart
                        // and the staff has no way to see
                        // anything.
                        // ============================================

                        CustomerDataStore.loadAll()


                        // Re-populate the staff in-memory
                        // lists (appointments, devices,
                        // payments) from Supabase too. The
                        // customer lists are what the home
                        // reads, but the Schedule / Devices
                        // / Payments screens read from
                        // StaffDataStore — without this,
                        // a reschedule done before the
                        // restart would revert to the
                        // old time.
                        // ============================================

                        StaffDataStore.loadAll()
                    }
                }
            }


            SessionState.LoggedOut,
            SessionState.Loading -> Unit
        }
    }


    when (sessionState) {

        SessionState.Loading -> {

            // ================================================
            // SPLASH
            //
            // Brief, themed splash while the persisted session
            // resolves. Real apps would brand this; ours just
            // shows a centred spinner on the theme background.
            // ================================================

            Box(
                modifier = Modifier.fillMaxSize(),

                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }
        }


        else -> {

            NekoEcoApp(
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,

                startDestination =
                    when (sessionState) {

                        is SessionState.LoggedInCustomer ->
                            "customer_home"

                        is SessionState.LoggedInStaff ->
                            "staff_home"

                        else ->
                            "role_selection"
                    }
            )
        }
    }
}


// ============================================================
// MAIN APP
// ============================================================

@Composable
fun NekoEcoApp(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    startDestination: String = "role_selection"
) {

    val navController =
        rememberNavController()


    NavHost(
        navController =
            navController,

        startDestination =
            startDestination
    ) {


        // ====================================================
        // ROLE SELECTION
        // ====================================================

        composable(
            "role_selection"
        ) {

            RoleSelectionScreen(

                darkMode =
                    darkMode,

                onDarkModeChange =
                    onDarkModeChange,

                onCustomerClick = {

                    navController.navigate(
                        "customer_login"
                    )
                },

                onStaffClick = {

                    navController.navigate(
                        "staff_login"
                    )
                }
            )
        }


        // ====================================================
        // CUSTOMER LOGIN
        // ====================================================

        composable(
            "customer_login"
        ) {

            CustomerLoginScreen(

                onLoginSuccess = {

                    navController.navigate(
                        "customer_home"
                    ) {

                        popUpTo(
                            "customer_login"
                        ) {
                            inclusive = true
                        }

                        launchSingleTop =
                            true
                    }
                },

                onSignUpClick = {

                    navController.navigate(
                        "customer_signup"
                    )
                },

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // ====================================================
        // CUSTOMER SIGN UP
        // ====================================================

        composable(
            "customer_signup"
        ) {

            CustomerSignUpScreen(

                onSignUpSuccess = {

                    navController.navigate(
                        "customer_home"
                    ) {

                        popUpTo(
                            "customer_login"
                        ) {
                            inclusive =
                                true
                        }

                        launchSingleTop =
                            true
                    }
                },

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // ====================================================
        // CUSTOMER MAIN
        // ====================================================

        composable(
            "customer_home"
        ) {

            CustomerMainScreen(

                startScreen =
                    "home",

                onLogout = {

                    CustomerDataStore
                        .logout()

                    navController.navigate(
                        "role_selection"
                    ) {

                        popUpTo(
                            "role_selection"
                        ) {
                            inclusive =
                                true
                        }

                        launchSingleTop =
                            true
                    }
                }
            )
        }


        // ====================================================
        // STAFF LOGIN
        // ====================================================

        composable(
            "staff_login"
        ) {

            StaffLoginScreen(

                onLoginSuccess = {

                    navController.navigate(
                        "staff_home"
                    ) {

                        popUpTo(
                            "staff_login"
                        ) {
                            inclusive =
                                true
                        }

                        launchSingleTop =
                            true
                    }
                },

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // ====================================================
        // STAFF MAIN
        // ====================================================

        composable(
            "staff_home"
        ) {

            StaffMainScreen(

                startScreen =
                    "home",

                onLogout = {

                    // ============================================
                    // Clear the in-memory staff session AND the
                    // persisted session so the next app launch
                    // lands on role_selection, not staff_home.
                    // ============================================

                    StaffAccounts.logout()


                    navController.navigate(
                        "role_selection"
                    ) {

                        popUpTo(
                            "role_selection"
                        ) {
                            inclusive =
                                true
                        }

                        launchSingleTop =
                            true
                    }
                }
            )
        }
    }
}