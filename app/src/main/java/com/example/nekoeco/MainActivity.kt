package com.example.nekoeco

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.nekoeco.ui.customer.CustomerDataStore
import com.example.nekoeco.ui.customer.CustomerLoginScreen
import com.example.nekoeco.ui.customer.CustomerMainScreen
import com.example.nekoeco.ui.customer.CustomerSignUpScreen

import com.example.nekoeco.ui.role.RoleSelectionScreen

import com.example.nekoeco.ui.staff.StaffLoginScreen
import com.example.nekoeco.ui.staff.StaffMainScreen

import com.example.nekoeco.ui.theme.NekoEcoTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        enableEdgeToEdge()


        setContent {

            var darkMode by remember {
                mutableStateOf(false)
            }


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

                    NekoEcoApp(
                        darkMode =
                            darkMode,

                        onDarkModeChange = {
                            darkMode = it
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// MAIN APP
// ============================================================

@Composable
fun NekoEcoApp(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {

    val navController =
        rememberNavController()


    NavHost(
        navController =
            navController,

        startDestination =
            "role_selection"
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