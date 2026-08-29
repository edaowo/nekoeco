package com.example.nekoeco.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.nekoeco.ai.AiScanResponse
import com.example.nekoeco.camera.CameraScreen
import com.example.nekoeco.result.AiResultScreen

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
        0
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
    // MAIN SCAFFOLD
    // ========================================================

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

                TopAppBar(

                    title = {

                        Text(

                            text =
                                "Neko Eco - Staff",

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimary,

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium
                        )
                    },

                    colors =
                        TopAppBarDefaults
                            .topAppBarColors(

                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                )
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
                "ai_result"
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


        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme
                            .colorScheme
                            .background
                    )
                    .padding(

                        if (
                            uiState.currentScreen ==
                            "ai_camera" ||

                            uiState.currentScreen ==
                            "ai_result"
                        ) {

                            PaddingValues()

                        } else {

                            innerPadding
                        }
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

                        onDevicesClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "devices"
                                    )
                            )
                        },

                        onAppointmentsClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },

                        onPaymentsClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "payments"
                                    )
                            )
                        },

                        onViewAllDevicesClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "devices"
                                    )
                            )
                        },

                        onLogout =
                            onLogout
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

                    StaffAppointmentsScreen()
                }


                // =================================================
                // PAYMENTS
                // =================================================

                "payments" -> {

                    StaffPaymentsScreen()
                }


                // =================================================
                // DEFAULT
                // =================================================

                else -> {

                    StaffHomeScreen(

                        onDevicesClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "devices"
                                    )
                            )
                        },

                        onAppointmentsClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },

                        onPaymentsClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "payments"
                                    )
                            )
                        },

                        onViewAllDevicesClick = {

                            viewModel.onEvent(
                                StaffMainEvent
                                    .ChangeScreen(
                                        "devices"
                                    )
                            )
                        },

                        onLogout =
                            onLogout
                    )
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
// NAVIGATION ITEM
// ============================================================

data class StaffNavigationItem(

    val route: String,

    val label: String,

    val icon: ImageVector,

    val selectedIcon: ImageVector
)