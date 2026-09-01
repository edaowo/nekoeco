package com.example.nekoeco.ui.customer

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nekoeco.R

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
        emptyList()
)


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerHomeEvent {

    data object RefreshHome :
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

    onAppointmentsClick: () -> Unit,

    onProfileClick: () -> Unit = {},

    viewModel: CustomerHomeViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // Subscribe to CustomerDataStore.dataVersion in the
    // composable body so Compose's snapshot system reliably
    // tracks the read. The LaunchedEffect below also reads
    // dataVersion, but if the home screen's body never reads
    // it directly, Compose can skip recomposing this
    // composable when the value changes — and the
    // LaunchedEffect's key wouldn't fire because it was
    // captured at initial composition.
    //
    // Reading it here forces a recomposition on every change,
    // which in turn re-evaluates the LaunchedEffect's key
    // and fires the refresh.
    // ========================================================

    val dataVersion = CustomerDataStore.dataVersion
    @Suppress("UNUSED_EXPRESSION")
    dataVersion


    // ========================================================
    // REFRESH WHEN HOME IS OPENED
    //
    // Keyed on CustomerDataStore.dataVersion so we re-read
    // our viewmodel snapshot every time the in-memory caches
    // are repopulated — e.g. after loadForCustomer completes
    // on app launch.
    // ========================================================

    LaunchedEffect(CustomerDataStore.dataVersion) {

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
        // GREETING + PROFILE ENTRY
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
                    "Hello, ${uiState.customerName} 👋",

                fontSize =
                    26.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,

                // Long names wrap/ellipsis instead of
                // pushing the profile circle off-screen.

                modifier =
                    Modifier.weight(1f),

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )


            Card(

                modifier =
                    Modifier
                        .size(44.dp)
                        .clickable {
                            onProfileClick()
                        },

                shape =
                    CircleShape,

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    )
            ) {

                Image(
                    painter =
                        painterResource(
                            R.drawable.profile_customer
                        ),

                    contentDescription =
                        "Profile",

                    contentScale =
                        ContentScale.Crop,

                    modifier =
                        Modifier.fillMaxSize()
                )
            }
        }



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

            CustomerSummaryCard(
                title = "Appointments",
                value =
                    uiState
                        .appointmentCount
                        .toString(),
                icon = Icons.Default.CalendarMonth,
                iconTint = Color(0xFF5B7CFA),
                iconBackground = Color(0xFFEAF0FF),
                modifier = Modifier.weight(1f)
            )


            CustomerSummaryCard(
                title = "Pending",
                value =
                    uiState
                        .pendingDeviceCount
                        .toString(),
                icon = Icons.Default.Inventory2,
                iconTint = Color(0xFF8B5CF6),
                iconBackground = Color(0xFFF1EBFF),
                modifier = Modifier.weight(1f)
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

            CustomerSummaryCard(
                title = "Completed\nPickups",
                value =
                    uiState
                        .completedPickupCount
                        .toString(),
                icon = Icons.Default.TaskAlt,
                iconTint = Color(0xFF3FA772),
                iconBackground = Color(0xFFE7F8F0),
                modifier = Modifier.weight(1f)
            )


            CustomerSummaryCard(
                title = "Total Received",
                value =
                    "RM %.2f".format(
                        uiState.totalReceived
                    ),
                icon = Icons.Default.CreditCard,
                iconTint = Color(0xFFE09B30),
                iconBackground = Color(0xFFFFF3DF),
                modifier = Modifier.weight(1f)
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


    }
}


// ============================================================
// CUSTOMER SUMMARY CARD
// ============================================================

@Composable
fun CustomerSummaryCard(

    title: String,

    value: String,

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
                Modifier.padding(10.dp)
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
                        13.sp,

                    lineHeight =
                        14.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Card(

                    shape =
                        RoundedCornerShape(8.dp),

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
                                .padding(5.dp)
                                .size(18.dp),

                        tint =
                            iconTint
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(

                text =
                    value,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
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