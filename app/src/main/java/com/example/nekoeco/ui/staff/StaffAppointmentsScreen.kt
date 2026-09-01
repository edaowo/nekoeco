package com.example.nekoeco.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

data class StaffAppointmentsUiState(

    val appointments: List<StaffAppointment> =
        emptyList(),

    val searchText: String =
        "",

    val selectedFilter: String =
        "All",

    val selectedAppointment: StaffAppointment? =
        null,

    val selectedDeviceRejected: Boolean =
        false,

    val showRescheduleDialog: Boolean =
        false

) {

    val visibleAppointments: List<StaffAppointment>
        get() =
            appointments.filter {
                it.status != "Pending"
            }


    val filteredAppointments: List<StaffAppointment>
        get() =
            visibleAppointments.filter { appointment ->

                val searchMatch =

                    appointment.customer.contains(
                        searchText,
                        ignoreCase = true
                    ) ||

                            appointment.device.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            appointment.company.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            appointment.id.contains(
                                searchText,
                                ignoreCase = true
                            )


                val statusMatch =

                    selectedFilter == "All" ||
                            appointment.status ==
                            selectedFilter


                searchMatch &&
                        statusMatch
            }
}


// ============================================================
// EVENTS
// ============================================================

sealed interface StaffAppointmentsEvent {

    data class SearchChanged(
        val text: String
    ) : StaffAppointmentsEvent


    data class FilterChanged(
        val filter: String
    ) : StaffAppointmentsEvent


    data class AppointmentSelected(
        val appointment: StaffAppointment
    ) : StaffAppointmentsEvent


    data object DismissDetails :
        StaffAppointmentsEvent


    data object AcceptAppointment :
        StaffAppointmentsEvent


    data object RejectAppointment :
        StaffAppointmentsEvent


    data object CompleteAppointment :
        StaffAppointmentsEvent


    data object OpenReschedule :
        StaffAppointmentsEvent


    data object DismissReschedule :
        StaffAppointmentsEvent


    data class RescheduleAppointment(
        val date: String,
        val time: String
    ) : StaffAppointmentsEvent


    data object RefreshAppointments :
        StaffAppointmentsEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class StaffAppointmentsViewModel(

    // Invoked after an appointment is successfully
    // marked Completed — StaffMainScreen uses it to
    // switch to the Payments tab for collection.
    private val onAppointmentCompleted:
            () -> Unit = {}

) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StaffAppointmentsUiState(
                appointments =
                    StaffDataStore
                        .appointments
                        .toList()
            )
        )


    val uiState: StateFlow<StaffAppointmentsUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: StaffAppointmentsEvent
    ) {

        when (event) {

            is StaffAppointmentsEvent.SearchChanged -> {

                _uiState.update {

                    it.copy(
                        searchText =
                            event.text
                    )
                }
            }


            is StaffAppointmentsEvent.FilterChanged -> {

                _uiState.update {

                    it.copy(
                        selectedFilter =
                            event.filter
                    )
                }
            }


            is StaffAppointmentsEvent.AppointmentSelected -> {

                val deviceRejected =

                    StaffDataStore
                        .devices
                        .any {

                            it.id ==
                                    event.appointment.deviceId &&

                                    it.status ==
                                    "Rejected"
                        }


                _uiState.update {

                    it.copy(

                        selectedAppointment =
                            event.appointment,

                        selectedDeviceRejected =
                            deviceRejected,

                        showRescheduleDialog =
                            false
                    )
                }
            }


            StaffAppointmentsEvent.DismissDetails -> {

                _uiState.update {

                    it.copy(

                        selectedAppointment =
                            null,

                        selectedDeviceRejected =
                            false,

                        showRescheduleDialog =
                            false
                    )
                }
            }


            StaffAppointmentsEvent.AcceptAppointment -> {

                acceptAppointment()
            }


            StaffAppointmentsEvent.RejectAppointment -> {

                rejectAppointment()
            }


            StaffAppointmentsEvent.CompleteAppointment -> {

                completeAppointment()
            }


            StaffAppointmentsEvent.OpenReschedule -> {

                _uiState.update {

                    it.copy(
                        showRescheduleDialog =
                            true
                    )
                }
            }


            StaffAppointmentsEvent.DismissReschedule -> {

                _uiState.update {

                    it.copy(
                        showRescheduleDialog =
                            false
                    )
                }
            }


            is StaffAppointmentsEvent.RescheduleAppointment -> {

                rescheduleAppointment(
                    date =
                        event.date,

                    time =
                        event.time
                )
            }


            StaffAppointmentsEvent.RefreshAppointments -> {

                refreshAppointments()
            }
        }
    }


    // ========================================================
    // ACCEPT
    // ========================================================

    private fun acceptAppointment() {

        val appointment =
            _uiState.value
                .selectedAppointment
                ?: return


        StaffDataStore.acceptAppointment(
            appointment.id
        )


        refreshAppointments()


        _uiState.update {

            it.copy(

                selectedAppointment =
                    null,

                selectedDeviceRejected =
                    false,

                showRescheduleDialog =
                    false
            )
        }
    }


    // ========================================================
    // REJECT
    // ========================================================

    private fun rejectAppointment() {

        val appointment =
            _uiState.value
                .selectedAppointment
                ?: return


        StaffDataStore.rejectAppointment(
            appointment.id
        )


        refreshAppointments()


        _uiState.update {

            it.copy(

                selectedAppointment =
                    null,

                selectedDeviceRejected =
                    false,

                showRescheduleDialog =
                    false
            )
        }
    }


    // ========================================================
    // COMPLETE
    // ========================================================

    private fun completeAppointment() {

        val appointment =
            _uiState.value
                .selectedAppointment
                ?: return


        StaffDataStore.completeAppointment(
            appointment.id
        )


        refreshAppointments()


        // Hand off to the Payments tab — the payment
        // for this device stays Pending until the
        // staff collects it there.

        onAppointmentCompleted()


        _uiState.update {

            it.copy(

                selectedAppointment =
                    null,

                selectedDeviceRejected =
                    false,

                showRescheduleDialog =
                    false
            )
        }
    }


    // ========================================================
    // RESCHEDULE
    // ========================================================

    private fun rescheduleAppointment(
        date: String,
        time: String
    ) {

        val appointment =
            _uiState.value
                .selectedAppointment
                ?: return


        StaffDataStore.rescheduleAppointment(
            appointment.id,
            date,
            time
        )


        refreshAppointments()


        _uiState.update {

            it.copy(

                selectedAppointment =
                    null,

                selectedDeviceRejected =
                    false,

                showRescheduleDialog =
                    false
            )
        }
    }


    // ========================================================
    // REFRESH
    // ========================================================

    private fun refreshAppointments() {

        val latestAppointments =

            StaffDataStore
                .appointments
                .toList()


        val currentSelected =
            _uiState.value
                .selectedAppointment


        val latestSelected =

            currentSelected?.let { selected ->

                latestAppointments.find {

                    it.id ==
                            selected.id
                }
            }


        val deviceRejected =

            latestSelected?.let { appointment ->

                StaffDataStore
                    .devices
                    .any {

                        it.id ==
                                appointment.deviceId &&

                                it.status ==
                                "Rejected"
                    }

            } ?: false


        _uiState.update {

            it.copy(

                appointments =
                    latestAppointments,

                selectedAppointment =
                    latestSelected,

                selectedDeviceRejected =
                    deviceRejected
            )
        }
    }
}


// ============================================================
// MAIN SCREEN
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaffAppointmentsScreen(

    onCompletedGoToPayments:
            () -> Unit = {},

    viewModel: StaffAppointmentsViewModel =
        viewModel {
            StaffAppointmentsViewModel(
                onAppointmentCompleted =
                    onCompletedGoToPayments
            )
        }

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {

        viewModel.onEvent(
            StaffAppointmentsEvent
                .RefreshAppointments
        )
    }


    val visibleAppointments =
        uiState.visibleAppointments


    val filteredAppointments =
        uiState.filteredAppointments


    Column(

        modifier =
            Modifier
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
                    horizontal = 20.dp
                )
    ) {

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        Text(
            text =
                "Schedule",

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold
        )


        Text(
            text =
                "${visibleAppointments.size} appointments",

            fontSize =
                13.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    if (uiState.searchText.isEmpty()) {
                        Text(
                            text = "Search appointment",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    BasicTextField(
                        value = uiState.searchText,
                        onValueChange = {
                            viewModel.onEvent(StaffAppointmentsEvent.SearchChanged(it))
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        FlowRow(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(6.dp),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            StaffAppointmentFilter(

                text = "All",

                selected =
                    uiState.selectedFilter ==
                            "All"

            ) {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .FilterChanged("All")
                )
            }


            StaffAppointmentFilter(

                text = "Scheduled",

                selected =
                    uiState.selectedFilter ==
                            "Scheduled"

            ) {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .FilterChanged("Scheduled")
                )
            }


            StaffAppointmentFilter(

                text = "Completed",

                selected =
                    uiState.selectedFilter ==
                            "Completed"

            ) {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .FilterChanged("Completed")
                )
            }


            StaffAppointmentFilter(

                text = "Cancelled",

                selected =
                    uiState.selectedFilter ==
                            "Cancelled"

            ) {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .FilterChanged("Cancelled")
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        Text(

            text =
                "${filteredAppointments.size} result(s)",

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Column(

            modifier =
                Modifier.fillMaxWidth(),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            filteredAppointments
                .forEach { appointment ->

                    StaffAppointmentCard(

                        appointment =
                            appointment,

                        onClick = {

                            viewModel.onEvent(
                                StaffAppointmentsEvent
                                    .AppointmentSelected(
                                        appointment
                                    )
                            )
                        }
                    )
                }


            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }


    // ========================================================
    // DETAILS
    // ========================================================

    val selectedAppointment =
        uiState.selectedAppointment


    if (
        selectedAppointment != null
    ) {

        StaffAppointmentDetailsDialog(

            appointment =
                selectedAppointment,

            deviceRejected =
                uiState.selectedDeviceRejected,

            onDismiss = {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .DismissDetails
                )
            },

            onAccept = {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .AcceptAppointment
                )
            },

            onReject = {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .RejectAppointment
                )
            },

            onComplete = {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .CompleteAppointment
                )
            },

            onReschedule = {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .OpenReschedule
                )
            }
        )
    }


    // ========================================================
    // RESCHEDULE DIALOG
    // ========================================================

    if (
        uiState.showRescheduleDialog &&
        selectedAppointment != null
    ) {

        AlertDialog(

            onDismissRequest = {

                viewModel.onEvent(
                    StaffAppointmentsEvent
                        .DismissReschedule
                )
            },

            title = {

                Text(
                    text =
                        "Reschedule Appointment"
                )
            },

            text = {

                Column {

                    // =========================================
                    // Compute the REAL tomorrow date — the
                    // options below used to hardcode a fixed
                    // "23 Aug 2026", so reschedules silently
                    // wrote a stale date to Supabase and the
                    // time appeared to revert after a
                    // refresh.
                    // =========================================

                    val tomorrowDate =
                        java.time.LocalDate.now()
                            .plusDays(1)
                            .format(
                                java.time.format.DateTimeFormatter
                                    .ofPattern("dd MMM yyyy")
                            )


                    Text(
                        text =
                            "Choose a new time:"
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    StaffRescheduleOption(
                        text =
                            "Tomorrow ($tomorrowDate) · 10:00"
                    ) {

                        viewModel.onEvent(
                            StaffAppointmentsEvent
                                .RescheduleAppointment(
                                    date =
                                        tomorrowDate,

                                    time =
                                        "10:00"
                                )
                        )
                    }


                    StaffRescheduleOption(
                        text =
                            "Tomorrow ($tomorrowDate) · 14:00"
                    ) {

                        viewModel.onEvent(
                            StaffAppointmentsEvent
                                .RescheduleAppointment(
                                    date =
                                        tomorrowDate,

                                    time =
                                        "14:00"
                                )
                        )
                    }


                    StaffRescheduleOption(
                        text =
                            "Tomorrow ($tomorrowDate) · 16:00"
                    ) {

                        viewModel.onEvent(
                            StaffAppointmentsEvent
                                .RescheduleAppointment(
                                    date =
                                        tomorrowDate,

                                    time =
                                        "16:00"
                                )
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        viewModel.onEvent(
                            StaffAppointmentsEvent
                                .DismissReschedule
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
// FILTER
// ============================================================

@Composable
fun StaffAppointmentFilter(

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

                        if (
                            text == "All"
                        )

                            20.dp

                        else

                            10.dp,

                    vertical =
                        5.dp
                )
    ) {

        Text(

            text =
                text,

            fontSize =
                11.sp,

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
// APPOINTMENT CARD
// ============================================================

@Composable
fun StaffAppointmentCard(

    appointment: StaffAppointment,

    onClick: () -> Unit

) {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },

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
                    3.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(18.dp)
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
                            .size(48.dp)
                            .clip(
                                RoundedCornerShape(
                                    14.dp
                                )
                            )
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.CalendarMonth,

                        contentDescription =
                            null,

                        tint =
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
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
                            appointment.device,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(

                        text =
                            appointment.id,

                        fontSize =
                            12.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                StaffAppointmentStatusBadge(
                    status =
                        appointment.status
                )
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            StaffAppointmentInfoRow(
                label = "Date",
                value = appointment.date
            )


            StaffAppointmentInfoRow(
                label = "Time",
                value = appointment.time
            )


            StaffAppointmentInfoRow(
                label = "Company",
                value = appointment.company
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            Text(

                text =
                    "Appointment ID: ${appointment.id}",

                fontSize =
                    10.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            TextButton(

                onClick =
                    onClick,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(

                    text =
                        "View Details →",

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }
    }
}


// ============================================================
// INFO ROW
// ============================================================

@Composable
fun StaffAppointmentInfoRow(

    label: String,

    value: String

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(

            text =
                label,

            fontSize =
                12.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Text(

            text =
                value,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Medium
        )
    }
}


// ============================================================
// STATUS
// ============================================================

@Composable
fun StaffAppointmentStatusBadge(

    status: String

) {

    val background =

        when (status) {

            "Scheduled",
            "Completed" ->

                MaterialTheme
                    .colorScheme
                    .primaryContainer


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

            "Scheduled",
            "Completed" ->

                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer


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

        text =
            status,

        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(50.dp)
                )
                .background(
                    background
                )
                .padding(
                    horizontal =
                        9.dp,

                    vertical =
                        5.dp
                ),

        fontSize =
            10.sp,

        fontWeight =
            FontWeight.Bold,

        color =
            textColor
    )
}


// ============================================================
// DETAILS DIALOG
// ============================================================

@Composable
fun StaffAppointmentDetailsDialog(

    appointment: StaffAppointment,

    deviceRejected: Boolean,

    onDismiss: () -> Unit,

    onAccept: () -> Unit,

    onReject: () -> Unit,

    onComplete: () -> Unit,

    onReschedule: () -> Unit

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

            // Scrollable so the details stay reachable on
            // landscape / short screens.

            Column(
                modifier =
                    Modifier.verticalScroll(
                        rememberScrollState()
                    )
            ) {

                StaffAppointmentDetail(
                    label = "Customer",
                    value = appointment.customer
                )


                StaffAppointmentDetail(
                    label = "Device",
                    value = appointment.device
                )


                StaffAppointmentDetail(
                    label = "Company",
                    value = appointment.company
                )


                StaffAppointmentDetail(
                    label = "Date",
                    value = appointment.date
                )


                StaffAppointmentDetail(
                    label = "Time",
                    value = appointment.time
                )


                StaffAppointmentDetail(
                    label = "Status",
                    value = appointment.status
                )


                Spacer(
                    modifier =
                        Modifier.height(15.dp)
                )


                // =================================================
                // SCHEDULED
                // =================================================

                if (
                    appointment.status ==
                    "Scheduled"
                ) {

                    Button(

                        onClick =
                            onComplete,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Check,

                            contentDescription =
                                null
                        )


                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )


                        Text(
                            text =
                                "Mark as Completed"
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(7.dp)
                    )


                    OutlinedButton(

                        onClick =
                            onReschedule,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Refresh,

                            contentDescription =
                                null
                        )


                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )


                        Text(
                            text =
                                "Reschedule"
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(7.dp)
                    )


                    OutlinedButton(

                        onClick =
                            onReject,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Close,

                            contentDescription =
                                null
                        )


                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )


                        Text(
                            text =
                                "Reject Appointment"
                        )
                    }
                }


                // =================================================
                // CANCELLED
                // =================================================

                if (
                    appointment.status ==
                    "Cancelled"
                ) {

                    if (
                        deviceRejected
                    ) {

                        Text(

                            text =
                                "This appointment was cancelled because the device was rejected. It cannot be rescheduled until the device is approved again.",

                            fontSize =
                                13.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )

                    } else {

                        Text(

                            text =
                                "This appointment was cancelled.",

                            fontSize =
                                13.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )


                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )


                        OutlinedButton(

                            onClick =
                                onReschedule,

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Refresh,

                                contentDescription =
                                    null
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )


                            Text(
                                text =
                                    "Reschedule"
                            )
                        }
                    }
                }


                // =================================================
                // COMPLETED
                // =================================================

                if (
                    appointment.status ==
                    "Completed"
                ) {

                    Text(

                        text =
                            "✓ This appointment has been completed.",

                        fontSize =
                            13.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }
        },

        confirmButton = {

            TextButton(

                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "Close"
                )
            }
        }
    )
}


// ============================================================
// DETAIL ROW
// ============================================================

@Composable
fun StaffAppointmentDetail(

    label: String,

    value: String

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(

            text =
                label,

            fontSize =
                12.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Text(

            text =
                value,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.SemiBold
        )
    }
}


// ============================================================
// RESCHEDULE OPTION
// ============================================================

@Composable
fun StaffRescheduleOption(

    text: String,

    onClick: () -> Unit

) {

    TextButton(

        onClick =
            onClick,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                text
        )
    }
}