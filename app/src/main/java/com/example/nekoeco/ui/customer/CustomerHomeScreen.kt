package com.example.nekoeco.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.TaskAlt

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.nekoeco.camera.BottomNavAction


// ============================================================
// UI STATE
// ============================================================

data class CustomerHomeUiState(

    val customerName: String =
        "Customer",

    val appointmentCount: Int =
        0,

    val pendingDeviceCount: Int =
        0,

    val completedPickupCount: Int =
        0,

    val totalReceived: Double =
        0.0,

    val upcomingAppointments:
    List<CustomerAppointment> =
        emptyList(),

    val showLogoutDialog: Boolean =
        false
)


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerHomeEvent {

    data object RefreshHome :
        CustomerHomeEvent

    data object LogoutClicked :
        CustomerHomeEvent

    data object DismissLogout :
        CustomerHomeEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerHomeViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerHomeUiState()
        )


    val uiState: StateFlow<CustomerHomeUiState> =
        _uiState.asStateFlow()


    init {

        refreshHome()
    }


    fun onEvent(
        event: CustomerHomeEvent
    ) {

        when (event) {

            CustomerHomeEvent.RefreshHome -> {

                refreshHome()
            }


            CustomerHomeEvent.LogoutClicked -> {

                _uiState.update {

                    it.copy(
                        showLogoutDialog = true
                    )
                }
            }


            CustomerHomeEvent.DismissLogout -> {

                _uiState.update {

                    it.copy(
                        showLogoutDialog = false
                    )
                }
            }
        }
    }


    private fun refreshHome() {

        val customer =
            CustomerDataStore.currentCustomer


        val devices =
            CustomerDataStore
                .getCurrentCustomerDevices()


        val appointments =
            CustomerDataStore
                .getCurrentCustomerAppointments()


        val payments =
            CustomerDataStore
                .getCurrentCustomerPayments()


        val appointmentCount =
            appointments.count {

                it.status ==
                        "Scheduled"
            }


        val pendingDeviceCount =
            devices.count {

                it.status == "Pending" ||
                        it.status == "Approved" ||
                        it.status == "Scheduled"
            }


        val completedPickupCount =
            devices.count {

                it.status ==
                        "Completed"
            }


        val totalReceived =
            payments
                .filter {

                    it.status ==
                            "Paid"
                }
                .sumOf {

                    it.amount
                }


        val upcomingAppointments =
            appointments
                .filter {

                    it.status ==
                            "Scheduled"
                }
                .take(2)


        _uiState.update {

            it.copy(

                customerName =
                    customer?.name
                        ?: "Customer",

                appointmentCount =
                    appointmentCount,

                pendingDeviceCount =
                    pendingDeviceCount,

                completedPickupCount =
                    completedPickupCount,

                totalReceived =
                    totalReceived,

                upcomingAppointments =
                    upcomingAppointments
            )
        }
    }
}


// ============================================================
// CUSTOMER HOME SCREEN
// ============================================================

@Composable
fun CustomerHomeScreen(

    onDevicesClick: () -> Unit,

    onAiScanClick: () -> Unit,

    onAppointmentsClick: () -> Unit,

    onLogout: () -> Unit,

    bottomNavActions:
    List<BottomNavAction>,

    viewModel: CustomerHomeViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // REFRESH WHEN HOME IS OPENED
    // ========================================================

    LaunchedEffect(Unit) {

        viewModel.onEvent(
            CustomerHomeEvent.RefreshHome
        )
    }


    // ========================================================
    // SCREEN
    // ========================================================

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme
                    .colorScheme
                    .background
            )
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            )
    ) {


        // ====================================================
        // GREETING
        // ====================================================

        Text(

            text =
                "Hello, ${uiState.customerName} 👋",

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )


        Spacer(
            modifier =
                Modifier.height(4.dp)
        )


        Text(

            text =
                "Welcome back to NekoEco",

            fontSize =
                14.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(22.dp)
        )


        // ====================================================
        // SUMMARY ROW 1
        // ====================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            CustomerSummaryCard(

                title =
                    "Appointments",

                value =
                    uiState
                        .appointmentCount
                        .toString(),

                subtitle =
                    "appointments",

                icon =
                    Icons.Default.CalendarMonth,

                iconTint =
                    Color(0xFF5B7CFA),

                iconBackground =
                    Color(0xFFEAF0FF),

                modifier =
                    Modifier.weight(1f)
            )


            CustomerSummaryCard(

                title =
                    "Pending",

                value =
                    uiState
                        .pendingDeviceCount
                        .toString(),

                subtitle =
                    "pending devices",

                icon =
                    Icons.Default.Inventory2,

                iconTint =
                    Color(0xFF8B5CF6),

                iconBackground =
                    Color(0xFFF1EBFF),

                modifier =
                    Modifier
                        .weight(1f)
                        .clickable {

                            onDevicesClick()
                        }
            )
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // ====================================================
        // SUMMARY ROW 2
        // ====================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            CustomerSummaryCard(

                title =
                    "Completed\nPickups",

                value =
                    uiState
                        .completedPickupCount
                        .toString(),

                subtitle =
                    "completed",

                icon =
                    Icons.Default.TaskAlt,

                iconTint =
                    Color(0xFF3FA772),

                iconBackground =
                    Color(0xFFE7F8F0),

                modifier =
                    Modifier.weight(1f)
            )


            CustomerSummaryCard(

                title =
                    "Total Received",

                value =
                    "RM %.0f".format(
                        uiState.totalReceived
                    ),

                subtitle =
                    "total payment",

                icon =
                    Icons.Default.CreditCard,

                iconTint =
                    Color(0xFFE09B30),

                iconBackground =
                    Color(0xFFFFF3DF),

                modifier =
                    Modifier.weight(1f)
            )
        }


        Spacer(
            modifier =
                Modifier.height(28.dp)
        )


        // ====================================================
        // UPCOMING PICKUP
        // ====================================================

        Text(

            text =
                "Upcoming Pickup",

            fontSize =
                18.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        if (
            uiState
                .upcomingAppointments
                .isEmpty()
        ) {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            1.dp
                    )
            ) {

                Text(

                    text =
                        "No upcoming pickup.",

                    modifier =
                        Modifier.padding(18.dp),

                    fontSize =
                        14.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

        } else {

            uiState
                .upcomingAppointments
                .forEachIndexed {
                        index,
                        appointment ->


                    CustomerPickupCard(

                        appointment =
                            appointment,

                        onClick =
                            onAppointmentsClick
                    )


                    if (
                        index <
                        uiState
                            .upcomingAppointments
                            .lastIndex
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )
                    }
                }
        }


        Spacer(
            modifier =
                Modifier.height(28.dp)
        )


        // ====================================================
        // LOGOUT CARD
        // ====================================================

        Card(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {

                        viewModel.onEvent(
                            CustomerHomeEvent
                                .LogoutClicked
                        )
                    },

            shape =
                RoundedCornerShape(16.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                ),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation =
                        1.dp
                )
        ) {

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(18.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(

                    imageVector =
                        Icons.Default.Logout,

                    contentDescription =
                        "Logout",

                    modifier =
                        Modifier.size(27.dp),

                    tint =
                        MaterialTheme
                            .colorScheme
                            .error
                )


                Spacer(
                    modifier =
                        Modifier.width(15.dp)
                )


                Column {

                    Text(

                        text =
                            "Logout",

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.SemiBold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )


                    Text(

                        text =
                            "Sign out of your account",

                        fontSize =
                            13.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )
    }


    // ========================================================
    // LOGOUT DIALOG
    // ========================================================

    if (
        uiState.showLogoutDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                viewModel.onEvent(
                    CustomerHomeEvent
                        .DismissLogout
                )
            },

            title = {

                Text(
                    text =
                        "Logout"
                )
            },

            text = {

                Text(
                    text =
                        "Are you sure you want to logout?"
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        viewModel.onEvent(
                            CustomerHomeEvent
                                .DismissLogout
                        )

                        onLogout()
                    }
                ) {

                    Text(
                        text =
                            "Logout"
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerHomeEvent
                                .DismissLogout
                        )
                    }
                ) {

                    Text(
                        text =
                            "Cancel"
                    )
                }
            }
        )
    }
}


// ============================================================
// CUSTOMER SUMMARY CARD
// ============================================================

@Composable
fun CustomerSummaryCard(

    title: String,

    value: String,

    subtitle: String,

    icon: ImageVector,

    iconTint: Color,

    iconBackground: Color,

    modifier: Modifier =
        Modifier

) {

    Card(

        modifier =
            modifier,

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    1.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(15.dp)
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text =
                        title,

                    fontSize =
                        14.sp,

                    lineHeight =
                        15.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Card(

                    shape =
                        RoundedCornerShape(9.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                iconBackground
                        )
                ) {

                    Icon(

                        imageVector =
                            icon,

                        contentDescription =
                            title,

                        modifier =
                            Modifier
                                .padding(7.dp)
                                .size(22.dp),

                        tint =
                            iconTint
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Text(

                text =
                    value,

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )


            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )


            Text(

                text =
                    subtitle,

                fontSize =
                    13.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// ============================================================
// CUSTOMER PICKUP CARD
// ============================================================

@Composable
fun CustomerPickupCard(

    appointment: CustomerAppointment,

    onClick: () -> Unit

) {

    val dateParts =
        appointment
            .date
            .split(" ")


    val day =
        dateParts
            .getOrNull(0)
            ?: "--"


    val month =
        dateParts
            .getOrNull(1)
            ?: "---"


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {

                    onClick()
                },

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    1.dp
            )
    ) {

        Row(

            modifier =
                Modifier.padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Card(

                modifier =
                    Modifier.size(56.dp),

                shape =
                    RoundedCornerShape(11.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(0xFFF1EBFF)
                    )
            ) {

                Column(

                    modifier =
                        Modifier.fillMaxSize(),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Text(

                        text =
                            day,

                        fontSize =
                            19.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFF8B5CF6)
                    )


                    Text(

                        text =
                            month,

                        fontSize =
                            13.sp,

                        color =
                            Color(0xFF8B5CF6)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )


            Column {

                Text(

                    text =
                        appointment.deviceName,

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )


                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )


                Text(

                    text =
                        appointment.time,

                    fontSize =
                        14.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )


                Text(

                    text =
                        appointment.company,

                    fontSize =
                        13.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}