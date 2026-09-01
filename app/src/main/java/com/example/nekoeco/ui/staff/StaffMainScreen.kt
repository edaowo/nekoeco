package com.example.nekoeco.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration

import com.example.nekoeco.ai.AiScanResponse
import com.example.nekoeco.camera.CameraScreen
import com.example.nekoeco.result.AiResultScreen

import com.example.nekoeco.ui.customer.CustomerDataStore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// ============================================================
// UI STATE
// ============================================================

data class StaffMainUiState(

    val currentScreen: String =
        "home",

    val aiResult: AiScanResponse? =
        null,

    val cameraSessionKey: Int =
        0,

    // True once the cold-launch data fetch
    // (CustomerDataStore.loadAll from MainActivity's
    // session restore) has completed. Used to show a
    // centred spinner over the dashboard until the first
    // batch of data is available — same as the customer
    // main screen.
    val dataLoaded: Boolean =
        false
)


// ============================================================
// EVENTS
// ============================================================

sealed interface StaffMainEvent {


    // ========================================================
    // NAVIGATION
    // ========================================================

    data class ChangeScreen(
        val screen: String
    ) : StaffMainEvent


    // ========================================================
    // CAMERA
    // ========================================================

    data object OpenCamera :
        StaffMainEvent


    data object BackFromCamera :
        StaffMainEvent


    // ========================================================
    // AI RESULT
    // ========================================================

    data class AiResultReceived(
        val result: AiScanResponse
    ) : StaffMainEvent


    data object BackFromResult :
        StaffMainEvent


    // Fired when CustomerDataStore.coldLaunchLoaded
    // flips to true (the cold-launch fetch from
    // MainActivity.AppRoot finished). Stops the
    // dashboard's loading spinner.
    data object DataLoaded :
        StaffMainEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class StaffMainViewModel(

    startScreen: String = "home"

) : ViewModel() {


    private val _uiState =
        MutableStateFlow(
            StaffMainUiState(
                currentScreen =
                    startScreen
            )
        )


    val uiState: StateFlow<StaffMainUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: StaffMainEvent
    ) {

        when (event) {


            // =================================================
            // CHANGE SCREEN
            // =================================================

            is StaffMainEvent.ChangeScreen -> {

                changeScreen(
                    event.screen
                )
            }


            // =================================================
            // OPEN CAMERA
            // =================================================

            StaffMainEvent.OpenCamera -> {

                openCamera()
            }


            // =================================================
            // BACK FROM CAMERA
            // =================================================

            StaffMainEvent.BackFromCamera -> {

                _uiState.update {

                    it.copy(
                        currentScreen =
                            "home",

                        aiResult =
                            null
                    )
                }
            }


            // =================================================
            // AI RESULT
            // =================================================

            is StaffMainEvent.AiResultReceived -> {

                _uiState.update {

                    it.copy(
                        currentScreen =
                            "ai_result",

                        aiResult =
                            event.result
                    )
                }
            }


            // =================================================
            // BACK FROM RESULT
            // =================================================

            StaffMainEvent.BackFromResult -> {

                _uiState.update {

                    it.copy(
                        currentScreen =
                            "home",

                        aiResult =
                            null
                    )
                }
            }


            // =================================================
            // DATA LOADED
            // =================================================

            StaffMainEvent.DataLoaded -> {

                _uiState.update {

                    it.copy(dataLoaded = true)
                }
            }
        }
    }


    // ========================================================
    // CHANGE SCREEN
    // ========================================================

    private fun changeScreen(
        screen: String
    ) {

        if (
            screen == "ai_camera"
        ) {

            openCamera()

            return
        }


        _uiState.update {

            it.copy(
                currentScreen =
                    screen,

                aiResult =
                    null
            )
        }
    }


    // ========================================================
    // OPEN CAMERA
    // ========================================================

    private fun openCamera() {

        _uiState.update {

            it.copy(
                currentScreen =
                    "ai_camera",

                aiResult =
                    null,

                cameraSessionKey =
                    it.cameraSessionKey + 1
            )
        }
    }
}


// ============================================================
// STAFF MAIN SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMainScreen(

    startScreen: String =
        "home",

    onLogout: () -> Unit,

    viewModel: StaffMainViewModel =
        viewModel {
            StaffMainViewModel(
                startScreen =
                    startScreen
            )
        }

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // ONLY CAMERA + AI RESULT ARE FULL SCREEN
    // ========================================================

    val isFullScreen =
        uiState.currentScreen == "ai_camera" ||
                uiState.currentScreen == "ai_result"


    // ========================================================
    // LANDSCAPE — SIDE RAIL
    //
    // In landscape the navigation items move to a left-hand
    // NavigationRail instead of the bottom bar.
    // ========================================================

    val isLandscape =
        LocalConfiguration.current.orientation ==
                Configuration.ORIENTATION_LANDSCAPE


    // ========================================================
    // OBSERVE COLD-LAUNCH LOAD COMPLETION
    //
    // CustomerDataStore.coldLaunchLoaded flips to true inside
    // loadAll once the first batch of data has been swapped
    // into the in-memory caches. Forward that into
    // StaffMainUiState so the dashboard stops showing its
    // loading spinner — same behaviour as CustomerMainScreen.
    // ========================================================

    val coldLaunchLoaded = CustomerDataStore.coldLaunchLoaded

    LaunchedEffect(coldLaunchLoaded) {

        if (coldLaunchLoaded) {

            viewModel.onEvent(
                StaffMainEvent.DataLoaded
            )
        }
    }


    // ========================================================
    // MAIN SCAFFOLD
    // ========================================================

    // ========================================================
    // LOAD ALL FROM SUPABASE ON ENTRY
    // ========================================================

    LaunchedEffect(Unit) {

        StaffDataStore.loadAll()
    }


    Scaffold(


        // ====================================================
        // TOP BAR
        // ====================================================

        topBar = {

            if (
                uiState.currentScreen !=
                "ai_camera" &&

                uiState.currentScreen !=
                "ai_result"
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = androidx.compose.ui.Alignment.BottomStart
                ) {
                    Text(
                        text = "Neko Eco - Staff",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }
            }
        },


        // ====================================================
        // BOTTOM NAVIGATION
        // ====================================================

        bottomBar = {

            if (
                uiState.currentScreen !=
                "ai_camera" &&

                uiState.currentScreen !=
                "ai_result" &&

                !isLandscape
            ) {

                StaffBottomNavigationBar(

                    currentScreen =
                        uiState.currentScreen,

                    onScreenChange = { screen ->

                        viewModel.onEvent(
                            StaffMainEvent
                                .ChangeScreen(
                                    screen
                                )
                        )
                    }
                )
            }
        },


        containerColor =
            MaterialTheme
                .colorScheme
                .background

    ) { innerPadding ->

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(

                        if (isFullScreen) {

                            PaddingValues()

                        } else if (isLandscape) {

                            // Drop the left/right system-bar
                            // insets in landscape — otherwise
                            // the scaffold colour shows as a
                            // strip left of the rail.

                            PaddingValues(
                                top =
                                    innerPadding.calculateTopPadding()
                            )

                        } else {

                            innerPadding
                        }
                    )
        ) {

            // ================================================
            // LANDSCAPE: SIDE RAIL INSTEAD OF BOTTOM BAR
            // ================================================

            if (isLandscape && !isFullScreen) {

                StaffNavigationRail(

                    currentScreen =
                        uiState.currentScreen,

                    onScreenChange = { screen ->

                        viewModel.onEvent(
                            StaffMainEvent
                                .ChangeScreen(
                                    screen
                                )
                        )
                    }
                )
            }


            Box(

                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            MaterialTheme
                                .colorScheme
                                .background
                        )
            ) {


            when (
                uiState.currentScreen
            ) {


                // =================================================
                // HOME
                // =================================================

                "home" -> {

                    StaffHomeScreen(

                        onAppointmentsClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },

                        onProfileClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "profile"
                                    )
                            )
                        }
                    )
                }


                // =================================================
                // DEVICES
                // =================================================

                "devices" -> {

                    StaffDevicesScreen(

                        onAiScanClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .OpenCamera
                            )
                        }
                    )
                }


                // =================================================
                // CAMERA
                // =================================================

                "ai_camera" -> {

                    key(
                        uiState.cameraSessionKey
                    ) {

                        CameraScreen(

                            onBack = {

                                viewModel.onEvent(
                                    StaffMainEvent
                                        .BackFromCamera
                                )
                            },

                            onResult = { result ->

                                viewModel.onEvent(
                                    StaffMainEvent
                                        .AiResultReceived(
                                            result
                                        )
                                )
                            },

                            bottomNavActions =
                                emptyList()
                        )
                    }
                }


                // =================================================
                // AI RESULT
                // =================================================

                "ai_result" -> {

                    val result =
                        uiState.aiResult


                    if (
                        result != null
                    ) {

                        AiResultScreen(

                            result =
                                result,

                            isCustomer =
                                false,

                            onBack = {

                                viewModel.onEvent(
                                    StaffMainEvent
                                        .BackFromResult
                                )
                            },

                            onBookPickup = {},

                            bottomNavActions =
                                emptyList()
                        )
                    }
                }


                // =================================================
                // APPOINTMENTS
                // =================================================

                "appointments" -> {

                    StaffAppointmentsScreen(

                        // After marking a pickup Completed,
                        // jump straight to the Payments tab
                        // to collect the payout.

                        onCompletedGoToPayments = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "payments"
                                    )
                            )
                        }
                    )
                }


                // =================================================
                // PAYMENTS
                // =================================================

                "payments" -> {

                    StaffPaymentsScreen()
                }


                // =================================================
                // PROFILE
                // =================================================

                "profile" -> {

                    StaffProfileScreen(

                        onBack = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "home"
                                    )
                            )
                        },

                        onLogout =
                            onLogout
                    )
                }


                // =================================================
                // DEFAULT
                // =================================================

                else -> {

                    StaffHomeScreen(

                        onAppointmentsClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },

                        onProfileClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "profile"
                                    )
                            )
                        }
                    )
                }
            }


            // ====================================================
            // COLD-LAUNCH LOADING OVERLAY
            //
            // Until the session-restore coroutine in
            // MainActivity.AppRoot finishes CustomerDataStore.loadAll(),
            // the dashboard would paint with all zeros. Cover the
            // content area with a centred spinner until dataLoaded
            // flips to true — same behaviour as the customer main
            // screen. Hidden on full-screen routes (camera + AI
            // result) so it doesn't conflict with their overlays.
            // ====================================================

            if (
                !uiState.dataLoaded &&
                !isFullScreen
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme
                                .colorScheme
                                .background
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),

                        color = MaterialTheme
                            .colorScheme
                            .primary,

                        strokeWidth = 5.dp
                    )
                }
            }
        }
        }
    }
}


// ============================================================
// STAFF BOTTOM NAVIGATION BAR
// ============================================================

@Composable
fun StaffBottomNavigationBar(

    currentScreen: String,

    onScreenChange: (String) -> Unit

) {

    val navigationItems =
        listOf(


            StaffNavigationItem(

                route =
                    "devices",

                label =
                    "Devices",

                icon =
                    Icons.Outlined.Inventory2,

                selectedIcon =
                    Icons.Filled.Inventory2
            ),


            StaffNavigationItem(

                route =
                    "ai_camera",

                label =
                    "Parts Analysis",

                icon =
                    Icons.Outlined.CameraAlt,

                selectedIcon =
                    Icons.Filled.CameraAlt
            ),


            StaffNavigationItem(

                route =
                    "home",

                label =
                    "Home",

                icon =
                    Icons.Outlined.Home,

                selectedIcon =
                    Icons.Filled.Home
            ),


            StaffNavigationItem(

                route =
                    "appointments",

                label =
                    "Schedule",

                icon =
                    Icons.Outlined.CalendarMonth,

                selectedIcon =
                    Icons.Filled.CalendarMonth
            ),


            StaffNavigationItem(

                route =
                    "payments",

                label =
                    "Payment",

                icon =
                    Icons.Outlined.CreditCard,

                selectedIcon =
                    Icons.Filled.CreditCard
            )
        )


    NavigationBar(

        containerColor =
            MaterialTheme
                .colorScheme
                .surface,

        tonalElevation =
            0.dp
    ) {

        navigationItems.forEach { item ->

            NavigationBarItem(

                selected =
                    currentScreen ==
                            item.route,

                onClick = {

                    onScreenChange(
                        item.route
                    )
                },

                icon = {

                    Icon(

                        imageVector =

                            if (
                                currentScreen ==
                                item.route
                            ) {

                                item.selectedIcon

                            } else {

                                item.icon
                            },

                        contentDescription =
                            item.label,

                        modifier =
                            Modifier.size(28.dp)
                    )
                },

                label = {

                    Text(

                        text =
                            item.label,

                        fontSize =
                            10.sp,

                        maxLines =
                            1
                    )
                },

                colors =
                    NavigationBarItemDefaults.colors(

                        selectedIconColor =
                            MaterialTheme
                                .colorScheme
                                .onPrimary,

                        selectedTextColor =
                            MaterialTheme
                                .colorScheme
                                .primary,

                        indicatorColor =
                            MaterialTheme
                                .colorScheme
                                .primary,

                        unselectedIconColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                        unselectedTextColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
            )
        }
    }
}


// ============================================================
// STAFF NAVIGATION RAIL (LANDSCAPE)
//
// Landscape counterpart of StaffBottomNavigationBar —
// same items, placed as a left-hand rail.
// ============================================================

@Composable
fun StaffNavigationRail(

    currentScreen: String,

    onScreenChange: (String) -> Unit

) {

    val navigationItems =
        listOf(

            StaffNavigationItem(
                route = "devices",
                label = "Devices",
                icon = Icons.Outlined.Inventory2,
                selectedIcon = Icons.Filled.Inventory2
            ),


            StaffNavigationItem(
                route = "ai_camera",
                label = "Parts Analysis",
                icon = Icons.Outlined.CameraAlt,
                selectedIcon = Icons.Filled.CameraAlt
            ),


            StaffNavigationItem(
                route = "home",
                label = "Home",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home
            ),


            StaffNavigationItem(
                route = "appointments",
                label = "Schedule",
                icon = Icons.Outlined.CalendarMonth,
                selectedIcon = Icons.Filled.CalendarMonth
            ),


            StaffNavigationItem(
                route = "payments",
                label = "Payment",
                icon = Icons.Outlined.CreditCard,
                selectedIcon = Icons.Filled.CreditCard
            )
        )


    NavigationRail(
        modifier =
            Modifier.width(110.dp),

        containerColor =
            MaterialTheme
                .colorScheme
                .background,

        // No system-bar insets — keeps the rail flush
        // against the left edge in landscape.
        windowInsets =
            WindowInsets(0.dp)
    ) {

        navigationItems.forEach { item ->

            NavigationRailItem(
                selected =
                    currentScreen ==
                            item.route,

                onClick = {

                    onScreenChange(
                        item.route
                    )
                },

                // Icon-only in landscape — the wording
                // stays on the bottom bar (portrait).

                icon = {

                    Icon(
                        imageVector =

                            if (currentScreen ==
                                item.route
                            ) {
                                item.selectedIcon
                            } else {
                                item.icon
                            },

                        contentDescription =
                            item.label,

                        modifier =
                            Modifier.size(26.dp)
                    )
                },

                colors =
                    NavigationRailItemDefaults.colors(
                        selectedIconColor =
                            MaterialTheme
                                .colorScheme
                                .onPrimary,

                        selectedTextColor =
                            MaterialTheme
                                .colorScheme
                                .primary,

                        indicatorColor =
                            MaterialTheme
                                .colorScheme
                                .primary,

                        unselectedIconColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                        unselectedTextColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
            )
        }
    }
}


// ============================================================
// NAVIGATION ITEM
// ============================================================

data class StaffNavigationItem(

    val route: String,

    val label: String,

    val icon: ImageVector,

    val selectedIcon: ImageVector
)