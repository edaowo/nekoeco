package com.example.nekoeco.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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


// ============================================================
// UI STATE
// ============================================================

data class CustomerAppointmentsUiState(

    val appointments: List<CustomerAppointment> =
        emptyList(),

    val selectedFilter: String =
        "All",

    val searchText: String =
        "",

    val selectedAppointment: CustomerAppointment? =
        null,

    val showDetailsDialog: Boolean =
        false,

    val showCancelDialog: Boolean =
        false

) {

    val filteredAppointments: List<CustomerAppointment>
        get() =

            appointments.filter { appointment ->

                val matchesFilter =

                    selectedFilter == "All" ||
                            appointment.status ==
                            selectedFilter


                val matchesSearch =

                    searchText.isBlank() ||

                            appointment.deviceName.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            appointment.company.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            appointment.appointmentId.contains(
                                searchText,
                                ignoreCase = true
                            )


                matchesFilter &&
                        matchesSearch
            }
}


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerAppointmentsEvent {

    data class SearchChanged(
        val text: String
    ) : CustomerAppointmentsEvent


    data class FilterChanged(
        val filter: String
    ) : CustomerAppointmentsEvent


    data class ViewDetails(
        val appointment: CustomerAppointment
    ) : CustomerAppointmentsEvent


    data class RequestCancel(
        val appointment: CustomerAppointment
    ) : CustomerAppointmentsEvent


    data object CancelFromDetails :
        CustomerAppointmentsEvent


    data object DismissDetails :
        CustomerAppointmentsEvent


    data object DismissCancel :
        CustomerAppointmentsEvent


    data object ConfirmCancel :
        CustomerAppointmentsEvent


    data object RefreshAppointments :
        CustomerAppointmentsEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerAppointmentsViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerAppointmentsUiState(
                appointments =
                    CustomerDataStore
                        .getCurrentCustomerAppointments()
            )
        )


    val uiState: StateFlow<CustomerAppointmentsUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: CustomerAppointmentsEvent
    ) {

        when (event) {

            is CustomerAppointmentsEvent.SearchChanged -> {

                _uiState.update {

                    it.copy(
                        searchText =
                            event.text
                    )
                }
            }


            is CustomerAppointmentsEvent.FilterChanged -> {

                _uiState.update {

                    it.copy(
                        selectedFilter =
                            event.filter
                    )
                }
            }


            is CustomerAppointmentsEvent.ViewDetails -> {

                _uiState.update {

                    it.copy(
                        selectedAppointment =
                            event.appointment,

                        showDetailsDialog =
                            true,

                        showCancelDialog =
                            false
                    )
                }
            }


            is CustomerAppointmentsEvent.RequestCancel -> {

                _uiState.update {

                    it.copy(
                        selectedAppointment =
                            event.appointment,

                        showDetailsDialog =
                            false,

                        showCancelDialog =
                            true
                    )
                }
            }


            CustomerAppointmentsEvent.CancelFromDetails -> {

                _uiState.update {

                    it.copy(
                        showDetailsDialog =
                            false,

                        showCancelDialog =
                            true
                    )
                }
            }


            CustomerAppointmentsEvent.DismissDetails -> {

                _uiState.update {

                    it.copy(
                        selectedAppointment =
                            null,

                        showDetailsDialog =
                            false
                    )
                }
            }


            CustomerAppointmentsEvent.DismissCancel -> {

                _uiState.update {

                    it.copy(
                        selectedAppointment =
                            null,

                        showCancelDialog =
                            false
                    )
                }
            }


            CustomerAppointmentsEvent.ConfirmCancel -> {

                cancelAppointment()
            }


            CustomerAppointmentsEvent.RefreshAppointments -> {

                refreshAppointments()
            }
        }
    }


    private fun cancelAppointment() {

        val appointment =
            _uiState.value
                .selectedAppointment
                ?: return


        CustomerDataStore.cancelAppointment(
            appointmentId =
                appointment.appointmentId
        )


        refreshAppointments()


        _uiState.update {

            it.copy(
                selectedAppointment =
                    null,

                showDetailsDialog =
                    false,

                showCancelDialog =
                    false
            )
        }
    }


    private fun refreshAppointments() {

        _uiState.update {

            it.copy(
                appointments =
                    CustomerDataStore
                        .getCurrentCustomerAppointments()
            )
        }
    }
}


// ============================================================
// SCREEN
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerAppointmentsScreen(

    viewModel: CustomerAppointmentsViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {

        viewModel.onEvent(
            CustomerAppointmentsEvent
                .RefreshAppointments
        )
    }


    val filteredAppointments =
        uiState.filteredAppointments


    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {

        Text(
            text = "Schedule",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme.colorScheme.onBackground
        )


        Spacer(
            modifier =
                Modifier.height(3.dp)
        )


        Text(
            text =
                "Manage your recycling appointments",

            fontSize = 13.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(18.dp)
        )


        OutlinedTextField(

            value =
                uiState.searchText,

            onValueChange = {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .SearchChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            leadingIcon = {

                Icon(
                    imageVector =
                        Icons.Default.Search,

                    contentDescription =
                        "Search"
                )
            },

            placeholder = {

                Text(
                    text =
                        "Search appointments..."
                )
            },

            shape =
                RoundedCornerShape(14.dp)
        )


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        FlowRow(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(6.dp),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            AppointmentFilterButton(
                text = "All",
                selected =
                    uiState.selectedFilter == "All"
            ) {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .FilterChanged("All")
                )
            }


            AppointmentFilterButton(
                text = "Scheduled",
                selected =
                    uiState.selectedFilter == "Scheduled"
            ) {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .FilterChanged("Scheduled")
                )
            }


            AppointmentFilterButton(
                text = "Completed",
                selected =
                    uiState.selectedFilter == "Completed"
            ) {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .FilterChanged("Completed")
                )
            }


            AppointmentFilterButton(
                text = "Cancelled",
                selected =
                    uiState.selectedFilter == "Cancelled"
            ) {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .FilterChanged("Cancelled")
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        Text(
            text =
                "${filteredAppointments.size} result(s)",

            fontSize = 13.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        if (filteredAppointments.isEmpty()) {

            EmptyAppointmentState()

        } else {

            LazyColumn(

                modifier =
                    Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(

                    items =
                        filteredAppointments,

                    key = {
                        it.appointmentId
                    }

                ) { appointment ->

                    CustomerAppointmentCard(

                        appointment =
                            appointment,

                        onViewDetails = {

                            viewModel.onEvent(
                                CustomerAppointmentsEvent
                                    .ViewDetails(
                                        appointment
                                    )
                            )
                        },

                        onCancel = {

                            viewModel.onEvent(
                                CustomerAppointmentsEvent
                                    .RequestCancel(
                                        appointment
                                    )
                            )
                        }
                    )
                }


                item {

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )
                }
            }
        }
    }


    val selectedAppointment =
        uiState.selectedAppointment


    if (
        uiState.showDetailsDialog &&
        selectedAppointment != null
    ) {

        CustomerAppointmentDetailsDialog(

            appointment =
                selectedAppointment,

            onDismiss = {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .DismissDetails
                )
            },

            onCancel = {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .CancelFromDetails
                )
            }
        )
    }


    if (
        uiState.showCancelDialog &&
        selectedAppointment != null
    ) {

        AlertDialog(

            onDismissRequest = {

                viewModel.onEvent(
                    CustomerAppointmentsEvent
                        .DismissCancel
                )
            },

            title = {

                Text(
                    text =
                        "Cancel Appointment?"
                )
            },

            text = {

                Text(
                    text =
                        "Are you sure you want to cancel appointment ${selectedAppointment.appointmentId}?"
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        viewModel.onEvent(
                            CustomerAppointmentsEvent
                                .ConfirmCancel
                        )
                    },

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                ) {

                    Text(
                        text =
                            "Cancel Appointment"
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerAppointmentsEvent
                                .DismissCancel
                        )
                    }
                ) {

                    Text(
                        text = "Keep"
                    )
                }
            }
        )
    }
}


// ============================================================
// FILTER
// ============================================================

@Composable
private fun AppointmentFilterButton(

    text: String,

    selected: Boolean,

    onClick: () -> Unit

) {

    Box(

        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(

                    if (selected)

                        MaterialTheme
                            .colorScheme
                            .primary

                    else

                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                )
                .clickable {
                    onClick()
                }
                .padding(

                    horizontal =
                        if (text == "All")
                            20.dp
                        else
                            10.dp,

                    vertical = 5.dp
                )
    ) {

        Text(

            text = text,

            fontSize = 11.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                if (selected)

                    MaterialTheme
                        .colorScheme
                        .onPrimary

                else

                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
        )
    }
}


// ============================================================
// CARD
// ============================================================

@Composable
private fun CustomerAppointmentCard(

    appointment: CustomerAppointment,

    onViewDetails: () -> Unit,

    onCancel: () -> Unit

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

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
                defaultElevation = 1.dp
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer,
                                RoundedCornerShape(12.dp)
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,

                        contentDescription = null,

                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )


                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            appointment.deviceName,

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(
                        text =
                            appointment.appointmentId,

                        fontSize = 11.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                CustomerAppointmentStatus(
                    status =
                        appointment.status
                )
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            AppointmentInfoRow(
                icon =
                    Icons.Default.Event,

                text =
                    "${appointment.date} • ${appointment.time}"
            )


            Spacer(
                modifier =
                    Modifier.height(7.dp)
            )


            AppointmentInfoRow(
                icon =
                    Icons.Default.Devices,

                text =
                    appointment.company
            )


            Spacer(
                modifier =
                    Modifier.height(7.dp)
            )


            AppointmentInfoRow(
                icon =
                    Icons.Default.LocationOn,

                text =
                    appointment.location
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(

                    onClick =
                        onViewDetails,

                    modifier =
                        Modifier.weight(1f),

                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Visibility,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(17.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.width(5.dp)
                    )


                    Text(
                        text = "Details"
                    )
                }


                if (
                    appointment.status ==
                    "Scheduled"
                ) {

                    OutlinedButton(

                        onClick =
                            onCancel,

                        modifier =
                            Modifier.weight(1f),

                        shape =
                            RoundedCornerShape(12.dp),

                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Close,
                            contentDescription =
                                null,
                            modifier =
                                Modifier.size(17.dp)
                        )


                        Spacer(
                            modifier =
                                Modifier.width(5.dp)
                        )


                        Text(
                            text = "Cancel"
                        )
                    }
                }
            }
        }
    }
}


// ============================================================
// INFO ROW
// ============================================================

@Composable
private fun AppointmentInfoRow(

    icon: ImageVector,

    text: String

) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier =
                Modifier.size(17.dp),
            tint =
                MaterialTheme
                    .colorScheme
                    .primary
        )


        Spacer(
            modifier =
                Modifier.width(8.dp)
        )


        Text(
            text = text,
            fontSize = 12.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}


// ============================================================
// STATUS
// ============================================================

@Composable
private fun CustomerAppointmentStatus(

    status: String

) {

    val backgroundColor =

        when (status) {

            "Scheduled" ->
                MaterialTheme
                    .colorScheme
                    .primaryContainer

            "Completed" ->
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer

            "Cancelled" ->
                MaterialTheme
                    .colorScheme
                    .errorContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        }


    val textColor =

        when (status) {

            "Scheduled" ->
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer

            "Completed" ->
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer

            "Cancelled" ->
                MaterialTheme
                    .colorScheme
                    .onErrorContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }


    Text(

        text = status,

        modifier =
            Modifier
                .background(
                    backgroundColor,
                    RoundedCornerShape(50.dp)
                )
                .padding(
                    horizontal = 9.dp,
                    vertical = 5.dp
                ),

        fontSize = 10.sp,

        fontWeight =
            FontWeight.Bold,

        color = textColor
    )
}


// ============================================================
// DETAILS
// ============================================================

@Composable
private fun CustomerAppointmentDetailsDialog(

    appointment: CustomerAppointment,

    onDismiss: () -> Unit,

    onCancel: () -> Unit

) {

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Appointment Details",

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                DetailRow(
                    "Appointment ID",
                    appointment.appointmentId
                )

                DetailRow(
                    "Device",
                    appointment.deviceName
                )

                DetailRow(
                    "Date",
                    appointment.date
                )

                DetailRow(
                    "Time",
                    appointment.time
                )

                DetailRow(
                    "Recycling Company",
                    appointment.company
                )

                DetailRow(
                    "Location",
                    appointment.location
                )

                DetailRow(
                    "Status",
                    appointment.status
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text = "Close"
                )
            }
        },

        dismissButton = {

            if (
                appointment.status ==
                "Scheduled"
            ) {

                TextButton(
                    onClick =
                        onCancel
                ) {

                    Text(
                        text =
                            "Cancel Appointment",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        }
    )
}


// ============================================================
// DETAIL ROW
// ============================================================

@Composable
private fun DetailRow(

    label: String,

    value: String

) {

    Column {

        Text(
            text = label,
            fontSize = 11.sp,
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
            text = value,
            fontSize = 14.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}


// ============================================================
// EMPTY
// ============================================================

@Composable
private fun EmptyAppointmentState() {

    Box(

        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    Icons.Default.CalendarMonth,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(50.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Text(
                text =
                    "No appointments found",
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )


            Text(
                text =
                    "Your recycling appointments will appear here.",
                fontSize = 12.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}