package com.example.nekoeco.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.nekoeco.ui.customer.CustomerDataStore
import com.example.nekoeco.ui.theme.EcoBlue
import com.example.nekoeco.ui.theme.EcoGreen
import com.example.nekoeco.ui.theme.EcoLightBlue
import com.example.nekoeco.ui.theme.EcoLightGreen
import com.example.nekoeco.ui.theme.EcoLightOrange
import com.example.nekoeco.ui.theme.EcoLightPurple
import com.example.nekoeco.ui.theme.EcoOrange
import com.example.nekoeco.ui.theme.EcoPurple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// ============================================================
// UI STATE
// ============================================================

data class StaffHomeUiState(

    val appointmentCount: Int = 0,

    val pendingDeviceCount: Int = 0,

    val completedPickupCount: Int = 0,

    val totalPaid: Double = 0.0,

    val upcomingPickups: List<StaffAppointment> =
        emptyList()
)


// ============================================================
// EVENTS
// ============================================================

sealed interface StaffHomeEvent {

    data object RefreshHome :
        StaffHomeEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class StaffHomeViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StaffHomeUiState()
        )

    val uiState: StateFlow<StaffHomeUiState> =
        _uiState.asStateFlow()


    init {

        refreshHome()
    }


    fun onEvent(
        event: StaffHomeEvent
    ) {

        when (event) {

            StaffHomeEvent.RefreshHome -> {

                refreshHome()
            }
        }
    }


    // ========================================================
    // REFRESH HOME
    // ========================================================

    private fun refreshHome() {

        // ====================================================
        // GET CUSTOMER DATA DIRECTLY
        //
        // IMPORTANT:
        // Staff Home now reads appointments directly from
        // CustomerDataStore.
        //
        // This avoids the problem where StaffDataStore has
        // not been updated yet.
        // ====================================================

        val customerAppointments =
            CustomerDataStore
                .appointments
                .toList()


        val customerDevices =
            CustomerDataStore
                .devices
                .toList()


        val customerPayments =
            CustomerDataStore
                .payments
                .toList()


        // ====================================================
        // CONVERT CUSTOMER APPOINTMENTS TO STAFF APPOINTMENTS
        // ====================================================

        val staffAppointments =
            customerAppointments.map { appointment ->

                val customerName =
                    getCustomerName(
                        appointment.customerId
                    )


                StaffAppointment(

                    id =
                        appointment.appointmentId,

                    customer =
                        customerName,

                    device =
                        appointment.deviceName,

                    deviceId =
                        appointment.deviceId,

                    company =
                        appointment.company,

                    date =
                        appointment.date,

                    time =
                        appointment.time,

                    location =
                        appointment.location,

                    status =
                        appointment.status
                )
            }


        // ====================================================
        // APPOINTMENT COUNT
        // ====================================================

        val appointmentCount =
            staffAppointments.count {

                it.status
                    .trim()
                    .equals(
                        "Scheduled",
                        ignoreCase = true
                    )
            }


        // ====================================================
        // PENDING DEVICES
        // ====================================================

        val pendingDeviceCount =
            customerDevices.count {

                it.status
                    .trim()
                    .equals(
                        "Pending",
                        ignoreCase = true
                    )
            }


        // ====================================================
        // COMPLETED PICKUPS
        // ====================================================

        val completedPickupCount =
            staffAppointments.count {

                it.status
                    .trim()
                    .equals(
                        "Completed",
                        ignoreCase = true
                    )
            }


        // ====================================================
        // TOTAL PAID
        // ====================================================

        val totalPaid =
            customerPayments
                .filter {

                    it.status
                        .trim()
                        .equals(
                            "Paid",
                            ignoreCase = true
                        )
                }
                .sumOf {

                    it.amount
                }


        // ====================================================
        // UPCOMING PICKUPS
        //
        // IMPORTANT:
        // Directly get Scheduled appointments from
        // CustomerDataStore.
        // ====================================================

        val upcomingPickups =
            staffAppointments
                .filter {

                    it.status
                        .trim()
                        .equals(
                            "Scheduled",
                            ignoreCase = true
                        )
                }
                .take(2)


        // ====================================================
        // UPDATE UI STATE
        // ====================================================

        _uiState.update {

            it.copy(

                appointmentCount =
                    appointmentCount,

                pendingDeviceCount =
                    pendingDeviceCount,

                completedPickupCount =
                    completedPickupCount,

                totalPaid =
                    totalPaid,

                upcomingPickups =
                    upcomingPickups
            )
        }
    }


    // ========================================================
    // GET CUSTOMER NAME
    // ========================================================

    private fun getCustomerName(
        customerId: String
    ): String {

        val currentCustomer =
            CustomerDataStore.currentCustomer


        return if (
            currentCustomer != null &&
            currentCustomer.customerId == customerId
        ) {

            currentCustomer.name

        } else {

            customerId
        }
    }
}


// ============================================================
// STAFF HOME SCREEN
// ============================================================

@Composable
fun StaffHomeScreen(

    onDevicesClick: () -> Unit = {},

    onAppointmentsClick: () -> Unit = {},

    onPaymentsClick: () -> Unit = {},

    onViewAllDevicesClick: () -> Unit = {},

    onLogout: () -> Unit = {},

    viewModel: StaffHomeViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // REFRESH WHEN HOME OPENS
    // ========================================================

    LaunchedEffect(Unit) {

        viewModel.onEvent(
            StaffHomeEvent.RefreshHome
        )
    }


    // ========================================================
    // SCREEN
    // ========================================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                )
    ) {


        // ====================================================
        // HEADER
        // ====================================================

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
                    "Home",

                fontSize =
                    32.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )


            Card(

                modifier =
                    Modifier.size(48.dp),

                shape =
                    CircleShape,

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .background
                    )
            ) {

                Icon(

                    imageVector =
                        Icons.Default.AccountCircle,

                    contentDescription =
                        "Staff Profile",

                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        // ====================================================
        // STATISTICS ROW 1
        // ====================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            StaffStatCard(

                title =
                    "Appointments",

                value =
                    uiState
                        .appointmentCount
                        .toString(),

                bottomText =
                    "appointments",

                icon =
                    Icons.Default.CalendarMonth,

                iconColor =
                    EcoBlue,

                iconBackground =
                    EcoLightBlue,

                modifier =
                    Modifier.weight(1f)
            )


            StaffStatCard(

                title =
                    "Pending",

                value =
                    uiState
                        .pendingDeviceCount
                        .toString(),

                bottomText =
                    "pending devices",

                icon =
                    Icons.Default.Inventory2,

                iconColor =
                    EcoPurple,

                iconBackground =
                    EcoLightPurple,

                modifier =
                    Modifier.weight(1f)
            )
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // ====================================================
        // STATISTICS ROW 2
        // ====================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            StaffStatCard(

                title =
                    "Completed Pickups",

                value =
                    uiState
                        .completedPickupCount
                        .toString(),

                bottomText =
                    "completed",

                icon =
                    Icons.Default.TaskAlt,

                iconColor =
                    EcoGreen,

                iconBackground =
                    EcoLightGreen,

                twoLineTitle =
                    true,

                modifier =
                    Modifier.weight(1f)
            )


            StaffStatCard(

                title =
                    "Total Paid",

                value =
                    "RM %.0f".format(
                        uiState.totalPaid
                    ),

                bottomText =
                    "total payment",

                icon =
                    Icons.Default.CreditCard,

                iconColor =
                    EcoOrange,

                iconBackground =
                    EcoLightOrange,

                modifier =
                    Modifier.weight(1f)
            )
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // ====================================================
        // UPCOMING PICKUP
        // ====================================================

        Text(

            text =
                "Upcoming Pickup",

            fontSize =
                20.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        // ====================================================
        // UPCOMING PICKUP LIST
        // ====================================================

        if (
            uiState
                .upcomingPickups
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
                    )
            ) {

                Text(

                    text =
                        "No upcoming pickups",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    modifier =
                        Modifier.padding(20.dp)
                )
            }

        } else {

            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f,
                            fill = false
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(

                    items =
                        uiState
                            .upcomingPickups,

                    key = {

                        it.id
                    }

                ) { appointment ->

                    UpcomingPickupCard(

                        appointment =
                            appointment
                    )
                }
            }
        }


        // ====================================================
        // LOGOUT
        // ====================================================

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        Card(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {

                        onLogout()
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
                            FontWeight.SemiBold,

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
}


// ============================================================
// STAT CARD
// ============================================================

@Composable
private fun StaffStatCard(

    title: String,

    value: String,

    bottomText: String,

    icon: ImageVector,

    iconColor: Color,

    iconBackground: Color,

    modifier: Modifier,

    twoLineTitle: Boolean =
        false

) {

    Card(

        modifier =
            modifier.height(145.dp),

        shape =
            RoundedCornerShape(18.dp),

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
                Modifier
                    .fillMaxSize()
                    .padding(14.dp)
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.Top
            ) {

                Text(

                    text =

                        if (
                            twoLineTitle
                        ) {

                            "Completed\nPickups"

                        } else {

                            title
                        },

                    fontSize =
                        12.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    fontWeight =
                        FontWeight.Medium,

                    lineHeight =
                        15.sp,

                    maxLines =

                        if (
                            twoLineTitle
                        )

                            2

                        else

                            1,

                    modifier =
                        Modifier.weight(1f)
                )


                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )


                Card(

                    modifier =
                        Modifier.size(46.dp),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                iconBackground
                        )
                ) {

                    BoxForIcon(

                        icon =
                            icon,

                        iconColor =
                            iconColor,

                        title =
                            title
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
                    25.sp,

                fontWeight =
                    FontWeight.Bold,

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
                    bottomText,

                fontSize =
                    12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// ============================================================
// ICON
// ============================================================

@Composable
private fun BoxForIcon(

    icon: ImageVector,

    iconColor: Color,

    title: String

) {

    Box(

        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Icon(

            imageVector =
                icon,

            contentDescription =
                title,

            tint =
                iconColor,

            modifier =
                Modifier.size(25.dp)
        )
    }
}


// ============================================================
// UPCOMING PICKUP CARD
// ============================================================

@Composable
private fun UpcomingPickupCard(

    appointment: StaffAppointment

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

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // =================================================
            // DATE
            // =================================================

            val dateParts =
                appointment
                    .date
                    .split(" ")


            val day =
                dateParts
                    .firstOrNull()
                    ?: "--"


            val month =
                dateParts
                    .getOrNull(1)
                    ?: "---"


            Card(

                modifier =
                    Modifier.size(60.dp),

                shape =
                    RoundedCornerShape(12.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            EcoLightPurple
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
                            21.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            EcoPurple
                    )


                    Text(

                        text =
                            month,

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.Medium,

                        color =
                            EcoPurple
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )


            // =================================================
            // APPOINTMENT INFO
            // =================================================

            Column(

                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        appointment.device,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )


                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )


                Text(

                    text =
                        appointment.customer,

                    fontSize =
                        13.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )


                Text(

                    text =
                        appointment.time,

                    fontSize =
                        12.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    fontWeight =
                        FontWeight.Medium
                )
            }
        }
    }
}