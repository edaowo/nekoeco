package com.example.nekoeco.ui.customer

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

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
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

import com.example.nekoeco.ui.theme.EcoCream

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

    val selectedDevice: CustomerDevice? = null,

    // Bumped whenever we want the devices list to
    // re-fetch from CustomerDataStore. Used as the
    // LaunchedEffect key in CustomerDevicesScreen so
    // it fires on every "return from details" event
    // (Compose can reuse the composable instance and
    // skip re-firing a Unit-keyed effect).
    val devicesRefreshTick: Int = 0,

    // True once the cold-launch data fetch (validate
    // session + load devices / appointments / payments)
    // has completed. Used to show a centred spinner over
    // the dashboard until the first batch of data is
    // available — otherwise the home tab paints with all
    // zeros while the network call is still in flight.
    val dataLoaded: Boolean = false
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


    // Fired from MainActivity.AppRoot once the
    // cold-launch data fetch has completed (or failed).
    // Flips CustomerMainUiState.dataLoaded to true so
    // the dashboard stops showing the spinner and
    // starts rendering the real data.
    data object DataLoaded :
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

                // ================================================
                // Mirror the appointments cancel flow: after the
                // data-store mutation, force the devices list to
                // re-read from the source of truth.
                //
                // mutableStateListOf.removeAll during the details
                // composition does NOT invalidate the devices
                // screen because that screen is not composed at
                // the moment the mutation happens. A fresh read
                // here, when we are about to flip back to the
                // devices branch, guarantees the row is gone.
                //
                // loadForCustomer clears the local lists first
                // and re-fetches from Supabase. By the time we
                // reach this point, either:
                //   - the server delete succeeded → row is gone
                //     server-side and locally;
                //   - the server delete failed → the data store
                //     has already restored the row locally, and
                //     the server still has it. loadForCustomer
                //     will pull it back, which is correct.
                // ================================================

                CustomerDataStore
                    .currentCustomer
                    ?.customerId
                    ?.let { cid ->
                        viewModelScope.launch {

                            CustomerDataStore
                                .loadForCustomer(cid)
                        }
                    }


                _uiState.update {

                    it.copy(
                        currentScreen = "devices",
                        selectedDevice = null,
                        devicesRefreshTick =
                            it.devicesRefreshTick + 1
                    )
                }
            }


            CustomerMainEvent.DataLoaded -> {

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

                // ============================================
                // Starts as Pending: the pickup is only
                // "Scheduled" once the staff inspects the
                // device and approves it.
                // ============================================

                status =
                    "Pending"
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
    // DATA LOAD TRIGGER
    //
    // The cold-launch load runs from MainActivity.AppRoot's
    // session-restore coroutine — right after currentCustomer
    // is set. This avoids the race where this composable
    // mounts before the session restore completes and the
    // launch effect would skip the load because
    // currentCustomerId was still null.
    //
    // The DeviceDeleted handler below also calls
    // loadForCustomer to refresh the in-memory caches after
    // a destructive action.
    // ========================================================


    // ========================================================
    // OBSERVE COLD-LAUNCH LOAD COMPLETION
    //
    // CustomerDataStore.coldLaunchLoaded flips to true inside
    // loadForCustomer / loadAll once the first batch of data
    // has been swapped into the in-memory caches. Forward
    // that into CustomerMainUiState so the dashboard stops
    // showing its loading spinner.
    // ========================================================

    val coldLaunchLoaded = CustomerDataStore.coldLaunchLoaded

    LaunchedEffect(coldLaunchLoaded) {

        if (coldLaunchLoaded) {

            viewModel.onEvent(
                CustomerMainEvent.DataLoaded
            )
        }
    }


    Scaffold(

        // ====================================================
        // TOP BAR
        // ====================================================

        topBar = {

            if (!isFullScreen) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = androidx.compose.ui.Alignment.BottomStart
                ) {
                    Text(
                        text = "Neko Eco - Customer",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }
            }
        },


        // ====================================================
        // BOTTOM NAV
        // ====================================================

        bottomBar = {

            if (!isFullScreen && !isLandscape) {

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

                CustomerNavigationRail(

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

                    CustomerHomeScreen(

                        onAppointmentsClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },


                        onProfileClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
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

                    CustomerDevicesScreen(

                        refreshKey =
                            uiState.devicesRefreshTick,

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

                            refreshKey =
                                uiState.devicesRefreshTick,

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
                // PROFILE
                // =================================================

                "profile" -> {

                    CustomerProfileScreen(

                        onBack = {

                            viewModel.onEvent(
                                CustomerMainEvent
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

                    CustomerHomeScreen(

                        onAppointmentsClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
                                    .ChangeScreen(
                                        "appointments"
                                    )
                            )
                        },


                        onProfileClick = {

                            viewModel.onEvent(
                                CustomerMainEvent
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
            // MainActivity.AppRoot finishes its parallel
            // Supabase fetches, the dashboard would paint
            // with all zeros. Cover the content area with a
            // modern centred spinner (the Material 3
            // CircularProgressIndicator) until dataLoaded
            // flips to true.
            //
            // We hide the spinner on full-screen routes
            // (camera + AI result) so it doesn't conflict
            // with their own overlays.
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
// CUSTOMER NAVIGATION RAIL (LANDSCAPE)
//
// Landscape counterpart of CustomerBottomNavigationBar —
// same items, placed as a left-hand rail.
// ============================================================

@Composable
fun CustomerNavigationRail(

    currentScreen: String,

    onScreenChange: (String) -> Unit

) {

    val navigationItems =
        listOf(

            CustomerNavigationItem(
                route = "devices",
                label = "Devices",
                icon = Icons.Outlined.Inventory2,
                selectedIcon = Icons.Filled.Inventory2
            ),


            CustomerNavigationItem(
                route = "ai_camera",
                label = "Parts Analysis",
                icon = Icons.Outlined.CameraAlt,
                selectedIcon = Icons.Filled.CameraAlt
            ),


            CustomerNavigationItem(
                route = "home",
                label = "Home",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home
            ),


            CustomerNavigationItem(
                route = "appointments",
                label = "Schedule",
                icon = Icons.Outlined.CalendarMonth,
                selectedIcon = Icons.Filled.CalendarMonth
            ),


            CustomerNavigationItem(
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
            Color.White,

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

data class CustomerNavigationItem(

    val route: String,

    val label: String,

    val icon: ImageVector,

    val selectedIcon: ImageVector
)