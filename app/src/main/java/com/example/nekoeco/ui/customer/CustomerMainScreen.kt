package com.example.nekoeco.ui.customer

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

import com.example.nekoeco.ui.theme.EcoCream
import com.example.nekoeco.ui.theme.EcoDarkGreen
import com.example.nekoeco.ui.theme.EcoWhite

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import java.time.LocalDate
import java.time.format.DateTimeFormatter


// ============================================================
// UI STATE
// ============================================================

data class CustomerMainUiState(

    val currentScreen: String = "home",

    val aiResult: AiScanResponse? = null,

    val cameraSessionKey: Int = 0,

    val selectedDevice: CustomerDevice? = null
)

// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerMainEvent {

    data class ChangeScreen(
        val screen: String
    ) : CustomerMainEvent


    data object OpenCamera :
        CustomerMainEvent


    data object BackFromCamera :
        CustomerMainEvent


    data class AiResultReceived(
        val result: AiScanResponse
    ) : CustomerMainEvent


    data object BackFromResult :
        CustomerMainEvent


    data object BookPickup :
        CustomerMainEvent


    data class DeviceSelected(
        val device: CustomerDevice
    ) : CustomerMainEvent

    data class ConfirmAppointment(
        val date: String,
        val time: String,
        val address: String
    ) : CustomerMainEvent

    data object BackFromDeviceDetails :
        CustomerMainEvent


    data object DeviceDeleted :
        CustomerMainEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerMainViewModel(

    startScreen: String = "home"

) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerMainUiState(
                currentScreen = startScreen
            )
        )


    val uiState: StateFlow<CustomerMainUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: CustomerMainEvent
    ) {

        when (event) {

            is CustomerMainEvent.ChangeScreen -> {

                changeScreen(
                    event.screen
                )
            }


            CustomerMainEvent.OpenCamera -> {

                openCamera()
            }


            CustomerMainEvent.BackFromCamera -> {

                _uiState.update {

                    it.copy(
                        currentScreen = "devices",
                        aiResult = null
                    )
                }
            }


            is CustomerMainEvent.AiResultReceived -> {

                _uiState.update {

                    it.copy(
                        currentScreen = "ai_result",
                        aiResult = event.result
                    )
                }
            }


            CustomerMainEvent.BackFromResult -> {

                _uiState.update {

                    it.copy(
                        currentScreen = "devices",
                        aiResult = null
                    )
                }
            }


            CustomerMainEvent.BookPickup -> {

                _uiState.update {

                    it.copy(
                        currentScreen = "booking_appointment"
                    )
                }
            }

            is CustomerMainEvent.ConfirmAppointment -> {

                confirmAppointment(
                    date = event.date,
                    time = event.time,
                    address = event.address
                )
            }

            is CustomerMainEvent.DeviceSelected -> {

                _uiState.update {

                    it.copy(
                        currentScreen = "device_details",
                        selectedDevice = event.device,
                        aiResult = null
                    )
                }
            }

            CustomerMainEvent.BackFromDeviceDetails -> {

                _uiState.update {

                    it.copy(
                        currentScreen = "devices",
                        selectedDevice = null
                    )
                }
            }


            CustomerMainEvent.DeviceDeleted -> {

                _uiState.update {

                    it.copy(
                        currentScreen = "devices",
                        selectedDevice = null
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
                currentScreen = screen,
                aiResult = null,
                selectedDevice = null
            )
        }
    }


    // ========================================================
    // OPEN CAMERA
    // ========================================================

    private fun openCamera() {

        _uiState.update {

            it.copy(
                currentScreen = "ai_camera",
                aiResult = null,
                selectedDevice = null,
                cameraSessionKey =
                    it.cameraSessionKey + 1
            )
        }
    }


    // ========================================================
// CONFIRM APPOINTMENT
// ========================================================

    private fun confirmAppointment(
        date: String,
        time: String,
        address: String
    ) {

        // ====================================================
        // GET AI RESULT
        // ====================================================

        val result =
            _uiState.value.aiResult
                ?: return


        // ====================================================
        // GET CURRENT CUSTOMER
        // ====================================================

        val currentCustomer =
            CustomerDataStore.currentCustomer
                ?: return


        // ====================================================
        // CURRENT DATE
        // ====================================================

        val currentDate =
            LocalDate.now().format(
                DateTimeFormatter.ofPattern(
                    "dd MMM yyyy"
                )
            )


        // ====================================================
        // GENERATE DEVICE ID
        // ====================================================

        val deviceId =
            CustomerDataStore.generateDeviceId()


        // ====================================================
        // CREATE CUSTOMER DEVICE
        // ====================================================

        val newDevice =
            CustomerDevice(

                deviceId =
                    deviceId,

                customerId =
                    currentCustomer.customerId,

                deviceName =
                    result.device.device,

                category =
                    "Electronic Device",

                date =
                    currentDate,

                // ============================================
                // NEW SCANNED DEVICE STARTS AS PENDING
                // ============================================

                status =
                    "Pending",

                accessories =
                    result.parts.map { part ->

                        CustomerAccessory(
                            name =
                                part.name,

                            price =
                                part.price
                        )
                    },

                recoveryValue =
                    result.totalRecoveryValue
            )


        // ====================================================
        // SAVE DEVICE
        //
        // CustomerDataStore.addDevice() will automatically
        // sync this device to StaffDataStore.
        // ====================================================

        CustomerDataStore.addDevice(
            newDevice
        )


        // ====================================================
        // GENERATE APPOINTMENT ID
        // ====================================================

        val appointmentId =
            CustomerDataStore.generateAppointmentId()


        // ====================================================
        // CREATE CUSTOMER APPOINTMENT
        // ====================================================

        val newAppointment =
            CustomerAppointment(

                appointmentId =
                    appointmentId,

                customerId =
                    currentCustomer.customerId,

                // IMPORTANT:
                // Use the SAME device ID
                // created above.
                deviceId =
                    newDevice.deviceId,

                deviceName =
                    newDevice.deviceName,

                date =
                    date,

                time =
                    time,

                company =
                    "NekoEco Recycling",

                location =
                    address,

                status =
                    "Scheduled"
            )


        // ====================================================
        // SAVE APPOINTMENT
        //
        // CustomerDataStore.addAppointment() will automatically
        // sync this appointment to StaffDataStore.
        // ====================================================

        CustomerDataStore.addAppointment(
            newAppointment
        )

        // ====================================================
        // GO TO CUSTOMER APPOINTMENTS
        // ====================================================

        _uiState.update {

            it.copy(

                currentScreen =
                    "appointments",

                aiResult =
                    null,

                selectedDevice =
                    null
            )
        }
    }
}

// ============================================================
// CUSTOMER MAIN SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMainScreen(

    startScreen: String = "home",

    onLogout: () -> Unit,

    viewModel: CustomerMainViewModel =
        viewModel {
            CustomerMainViewModel(
                startScreen = startScreen
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


    Scaffold(

        // ====================================================
        // TOP BAR
        // ====================================================

        topBar = {

            if (!isFullScreen) {

                TopAppBar(

                    title = {

                        Text(
                            text = "Neko Eco - Customer",
                            color = EcoWhite,
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
                                    EcoDarkGreen
                            )
                )
            }
        },


        // ====================================================
        // BOTTOM NAV
        // ====================================================

        bottomBar = {

            if (!isFullScreen) {

                CustomerBottomNavigationBar(

                    currentScreen =
                        uiState.currentScreen,

                    onScreenChange = { screen ->

                        viewModel.onEvent(
                            CustomerMainEvent
                                .ChangeScreen(
                                    screen
                                )
                        )
                    }
                )
            }
        },


        containerColor =
            EcoCream

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

                        if (isFullScreen) {

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

                    CustomerHomeScreen(

                        onDevicesClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .ChangeScreen(
                                        "devices"
                                    )
                            )
                        },


                        onAiScanClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .OpenCamera
                            )
                        },


                        onAppointmentsClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },


                        onLogout =
                            onLogout,


                        bottomNavActions =
                            emptyList()
                    )
                }


                // =================================================
                // DEVICES
                // =================================================

                "devices" -> {

                    CustomerDevicesScreen(

                        onAddDevice = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .OpenCamera
                            )
                        },


                        onDeviceClick = { device ->

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .DeviceSelected(
                                        device
                                    )
                            )
                        }
                    )
                }


                // =================================================
                // DEVICE DETAILS
                // =================================================

                "device_details" -> {

                    val selectedDevice =
                        uiState.selectedDevice


                    if (
                        selectedDevice != null
                    ) {

                        CustomerDeviceDetailsScreen(

                            device =
                                selectedDevice,


                            onBackClick = {

                                viewModel.onEvent(
                                    CustomerMainEvent
                                        .BackFromDeviceDetails
                                )
                            },


                            onDeviceDeleted = {

                                viewModel.onEvent(
                                    CustomerMainEvent
                                        .DeviceDeleted
                                )
                            }
                        )

                    } else {

                        CustomerDevicesScreen(

                            onAddDevice = {

                                viewModel.onEvent(
                                    CustomerMainEvent
                                        .OpenCamera
                                )
                            },


                            onDeviceClick = { device ->

                                viewModel.onEvent(
                                    CustomerMainEvent
                                        .DeviceSelected(
                                            device
                                        )
                                )
                            }
                        )
                    }
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
                                    CustomerMainEvent
                                        .BackFromCamera
                                )
                            },


                            onResult = { result ->

                                viewModel.onEvent(
                                    CustomerMainEvent
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
                                true,


                            onBack = {

                                viewModel.onEvent(
                                    CustomerMainEvent
                                        .BackFromResult
                                )
                            },


                            onBookPickup = {

                                viewModel.onEvent(
                                    CustomerMainEvent
                                        .BookPickup
                                )
                            },


                            bottomNavActions =
                                emptyList()
                        )
                    }
                }

                // =================================================
                // BOOKING APPOINTMENTS
                // =================================================
                "booking_appointment" -> {

                    val result =
                        uiState.aiResult

                    if (result != null) {

                        BookingAppointmentScreen(

                            result = result,

                            onBack = {

                                viewModel.onEvent(
                                    CustomerMainEvent.BackFromResult
                                )
                            },

                            onConfirm = { date, time, address ->

                                viewModel.onEvent(
                                    CustomerMainEvent.ConfirmAppointment(
                                        date = date,
                                        time = time,
                                        address = address
                                    )
                                )
                            }
                        )
                    }
                }


                // =================================================
                // APPOINTMENTS
                // =================================================

                "appointments" -> {

                    CustomerAppointmentsScreen()
                }


                // =================================================
                // PAYMENTS
                // =================================================

                "payments" -> {

                    CustomerPaymentsScreen()
                }


                // =================================================
                // DEFAULT
                // =================================================

                else -> {

                    CustomerHomeScreen(

                        onDevicesClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .ChangeScreen(
                                        "devices"
                                    )
                            )
                        },


                        onAiScanClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .OpenCamera
                            )
                        },


                        onAppointmentsClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },


                        onLogout =
                            onLogout,


                        bottomNavActions =
                            emptyList()
                    )
                }
            }
        }
    }
}


// ============================================================
// CUSTOMER BOTTOM NAVIGATION
// ============================================================

@Composable
fun CustomerBottomNavigationBar(

    currentScreen: String,

    onScreenChange: (String) -> Unit

) {

    val navigationItems =
        listOf(


            CustomerNavigationItem(

                route =
                    "devices",

                label =
                    "Devices",

                icon =
                    Icons.Outlined.Inventory2,

                selectedIcon =
                    Icons.Filled.Inventory2
            ),


            CustomerNavigationItem(

                route =
                    "ai_camera",

                label =
                    "Parts Analysis",

                icon =
                    Icons.Outlined.CameraAlt,

                selectedIcon =
                    Icons.Filled.CameraAlt
            ),


            CustomerNavigationItem(

                route =
                    "home",

                label =
                    "Home",

                icon =
                    Icons.Outlined.Home,

                selectedIcon =
                    Icons.Filled.Home
            ),


            CustomerNavigationItem(

                route =
                    "appointments",

                label =
                    "Schedule",

                icon =
                    Icons.Outlined.CalendarMonth,

                selectedIcon =
                    Icons.Filled.CalendarMonth
            ),


            CustomerNavigationItem(

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

data class CustomerNavigationItem(

    val route: String,

    val label: String,

    val icon: ImageVector,

    val selectedIcon: ImageVector
)