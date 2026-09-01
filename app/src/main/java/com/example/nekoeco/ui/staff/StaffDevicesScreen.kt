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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

data class StaffDevicesUiState(

    val devices: List<StaffDevice> =
        emptyList(),

    val searchText: String =
        "",

    val selectedFilter: String =
        "All",

    val selectedDevice: StaffDevice? =
        null,

    val currentStatus: String =
        "",

    val showDeleteDialog: Boolean =
        false,

    // Component names ticked in the inspection
    // checkboxes for the device currently open in the
    // details view. Seeded from the device's saved
    // approved flags when it is selected.
    val inspectionSelection: Set<String> =
        emptySet()

) {

    val filteredDevices: List<StaffDevice>
        get() =

            devices.filter { device ->

                val searchMatch =

                    device.customer.contains(
                        searchText,
                        ignoreCase = true
                    ) ||

                            device.deviceName.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            device.category.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            device.id.contains(
                                searchText,
                                ignoreCase = true
                            )


                val statusMatch =

                    selectedFilter == "All" ||
                            device.status ==
                            selectedFilter


                searchMatch &&
                        statusMatch
            }
}


// ============================================================
// EVENTS
// ============================================================

sealed interface StaffDevicesEvent {


    // ========================================================
    // LIST
    // ========================================================

    data class SearchChanged(
        val text: String
    ) : StaffDevicesEvent


    data class FilterChanged(
        val filter: String
    ) : StaffDevicesEvent


    data object RefreshDevices :
        StaffDevicesEvent


    // ========================================================
    // DEVICE DETAILS
    // ========================================================

    data class DeviceSelected(
        val device: StaffDevice
    ) : StaffDevicesEvent


    data object BackFromDetails :
        StaffDevicesEvent


    // ========================================================
    // STATUS
    // ========================================================

    data class StatusChanged(
        val status: String
    ) : StaffDevicesEvent


    // ========================================================
    // INSPECTION
    // ========================================================

    data class InspectionToggled(
        val componentName: String
    ) : StaffDevicesEvent


    data class InspectionSelectAll(
        val selected: Boolean
    ) : StaffDevicesEvent


    // ========================================================
    // DELETE
    // ========================================================

    data object DeleteClicked :
        StaffDevicesEvent


    data object DismissDelete :
        StaffDevicesEvent


    data object ConfirmDelete :
        StaffDevicesEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class StaffDevicesViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StaffDevicesUiState(
                devices =
                    StaffDataStore.devices.toList()
            )
        )


    val uiState: StateFlow<StaffDevicesUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: StaffDevicesEvent
    ) {

        when (event) {


            // =================================================
            // SEARCH
            // =================================================

            is StaffDevicesEvent.SearchChanged -> {

                _uiState.update {

                    it.copy(
                        searchText =
                            event.text
                    )
                }
            }


            // =================================================
            // FILTER
            // =================================================

            is StaffDevicesEvent.FilterChanged -> {

                _uiState.update {

                    it.copy(
                        selectedFilter =
                            event.filter
                    )
                }
            }


            // =================================================
            // REFRESH
            // =================================================

            StaffDevicesEvent.RefreshDevices -> {

                refreshDevices()
            }


            // =================================================
            // SELECT DEVICE
            // =================================================

            is StaffDevicesEvent.DeviceSelected -> {

                _uiState.update {

                    it.copy(
                        selectedDevice =
                            event.device,

                        currentStatus =
                            event.device.status,

                        showDeleteDialog =
                            false,

                        inspectionSelection =

                            // Seed the checkboxes from the
                            // device's saved approved flags.

                            event.device.components
                                .filter { it.approved }
                                .map { it.name }
                                .toSet()
                    )
                }
            }


            // =================================================
            // BACK
            // =================================================

            StaffDevicesEvent.BackFromDetails -> {

                refreshDevices()

                _uiState.update {

                    it.copy(
                        selectedDevice =
                            null,

                        currentStatus =
                            "",

                        showDeleteDialog =
                            false
                    )
                }
            }


            // =================================================
            // CHANGE STATUS
            // =================================================

            is StaffDevicesEvent.StatusChanged -> {

                updateStatus(
                    event.status
                )
            }


            // =================================================
            // INSPECTION
            // =================================================

            is StaffDevicesEvent.InspectionToggled -> {

                _uiState.update {

                    val selection =
                        it.inspectionSelection
                            .toMutableSet()


                    if (
                        selection.contains(
                            event.componentName
                        )
                    ) {

                        selection.remove(
                            event.componentName
                        )

                    } else {

                        selection.add(
                            event.componentName
                        )
                    }


                    it.copy(
                        inspectionSelection =
                            selection
                    )
                }
            }


            is StaffDevicesEvent.InspectionSelectAll -> {

                _uiState.update {

                    val allNames =

                        it.selectedDevice
                            ?.components
                            ?.map { component ->
                                component.name
                            }
                            ?.toSet()
                            ?: emptySet()


                    it.copy(
                        inspectionSelection =

                            if (event.selected) {
                                allNames
                            } else {
                                emptySet()
                            }
                    )
                }
            }


            // =================================================
            // DELETE
            // =================================================

            StaffDevicesEvent.DeleteClicked -> {

                _uiState.update {

                    it.copy(
                        showDeleteDialog =
                            true
                    )
                }
            }


            StaffDevicesEvent.DismissDelete -> {

                _uiState.update {

                    it.copy(
                        showDeleteDialog =
                            false
                    )
                }
            }


            StaffDevicesEvent.ConfirmDelete -> {

                deleteDevice()
            }
        }
    }


    // ========================================================
    // REFRESH
    // ========================================================

    private fun refreshDevices() {

        val latestDevices =
            StaffDataStore
                .devices
                .toList()


        val selectedId =
            _uiState.value
                .selectedDevice
                ?.id


        val latestSelectedDevice =
            if (selectedId != null) {

                latestDevices.find {
                    it.id == selectedId
                }

            } else {

                null
            }


        _uiState.update {

            it.copy(
                devices =
                    latestDevices,

                selectedDevice =
                    latestSelectedDevice
                        ?: it.selectedDevice
            )
        }
    }


    // ========================================================
    // UPDATE STATUS
    // ========================================================

    private fun updateStatus(
        newStatus: String
    ) {

        val selectedDevice =
            _uiState.value
                .selectedDevice
                ?: return


        if (
            newStatus == "Approved" &&
            selectedDevice.components.isNotEmpty()
        ) {

            // ================================================
            // Approval always goes through the inspection
            // selection: persist the ticked parts + their
            // recomputed total first, then run the standard
            // approval chain (appointment → Scheduled,
            // payment created from the new total).
            // ================================================

            StaffDataStore.approveDeviceWithInspection(
                deviceId =
                    selectedDevice.id,

                approvedNames =
                    _uiState.value.inspectionSelection
            )

        } else {

            StaffDataStore.updateDeviceStatus(
                selectedDevice.id,
                newStatus
            )
        }


        val latestDevices =
            StaffDataStore
                .devices
                .toList()


        val updatedDevice =
            latestDevices.find {

                it.id ==
                        selectedDevice.id
            }


        _uiState.update {

            it.copy(
                devices =
                    latestDevices,

                selectedDevice =
                    updatedDevice
                        ?: selectedDevice,

                currentStatus =
                    newStatus
            )
        }
    }


    // ========================================================
    // DELETE DEVICE
    // ========================================================

    private fun deleteDevice() {

        val selectedDevice =
            _uiState.value
                .selectedDevice
                ?: return


        StaffDataStore.deleteDevice(
            selectedDevice.id
        )


            _uiState.update {

                it.copy(
                    devices =
                        StaffDataStore
                            .devices
                            .toList(),

                    selectedDevice =
                        null,

                    currentStatus =
                        "",

                    showDeleteDialog =
                        false
                )
            }
    }
}


// ============================================================
// STAFF DEVICES SCREEN
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaffDevicesScreen(

    onAiScanClick: () -> Unit,

    viewModel: StaffDevicesViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // REFRESH WHEN OPENED
    // ========================================================

    LaunchedEffect(Unit) {

        viewModel.onEvent(
            StaffDevicesEvent.RefreshDevices
        )
    }


    // ========================================================
    // DEVICE DETAILS
    // ========================================================

    val selectedDevice =
        uiState.selectedDevice


    if (
        selectedDevice != null
    ) {

        StaffDeviceDetailsScreen(

            device =
                selectedDevice,

            currentStatus =
                uiState.currentStatus,

            showDeleteDialog =
                uiState.showDeleteDialog,

            inspectionSelection =
                uiState.inspectionSelection,

            onEvent =
                viewModel::onEvent
        )


        return
    }


    // ========================================================
    // FILTERED DEVICES
    // ========================================================

    val filteredDevices =
        uiState.filteredDevices


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


        // ====================================================
        // TITLE
        // ====================================================

        Text(
            text =
                "Devices",

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold
        )


        Text(
            text =
                "Manage customer devices",

            fontSize =
                13.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        // ====================================================
        // SEARCH
        // ====================================================

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
                            text = "Search devices",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    BasicTextField(
                        value = uiState.searchText,
                        onValueChange = {
                            viewModel.onEvent(StaffDevicesEvent.SearchChanged(it))
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


        // ====================================================
        // FILTERS
        // ====================================================

        FlowRow(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(6.dp),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            DeviceFilter(
                text =
                    "All",

                selected =
                    uiState.selectedFilter ==
                            "All"
            ) {

                viewModel.onEvent(
                    StaffDevicesEvent
                        .FilterChanged("All")
                )
            }


            DeviceFilter(
                text =
                    "Pending",

                selected =
                    uiState.selectedFilter ==
                            "Pending"
            ) {

                viewModel.onEvent(
                    StaffDevicesEvent
                        .FilterChanged("Pending")
                )
            }


            DeviceFilter(
                text =
                    "Approved",

                selected =
                    uiState.selectedFilter ==
                            "Approved"
            ) {

                viewModel.onEvent(
                    StaffDevicesEvent
                        .FilterChanged("Approved")
                )
            }


            DeviceFilter(
                text =
                    "Rejected",

                selected =
                    uiState.selectedFilter ==
                            "Rejected"
            ) {

                viewModel.onEvent(
                    StaffDevicesEvent
                        .FilterChanged("Rejected")
                )
            }


            DeviceFilter(
                text =
                    "Completed",

                selected =
                    uiState.selectedFilter ==
                            "Completed"
            ) {

                viewModel.onEvent(
                    StaffDevicesEvent
                        .FilterChanged("Completed")
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        // ====================================================
        // RESULT COUNT
        // ====================================================

        Text(

            text =
                "${filteredDevices.size} device(s)",

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


        // ====================================================
        // DEVICE LIST
        // ====================================================

        Column(

            modifier =
                Modifier.fillMaxWidth(),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            filteredDevices
                .forEach { device ->

                    DeviceCard(

                        device =
                            device,

                        onClick = {

                            viewModel.onEvent(
                                StaffDevicesEvent
                                    .DeviceSelected(
                                        device
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
}


// ============================================================
// FILTER BUTTON
// ============================================================

@Composable
fun DeviceFilter(

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
// DEVICE CARD
// ============================================================

@Composable
fun DeviceCard(

    device: StaffDevice,

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
                            .size(52.dp)
                            .clip(
                                RoundedCornerShape(
                                    15.dp
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

                    DeviceIcon(
                        category =
                            device.category
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
                            device.deviceName,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(
                        text =
                            device.category,

                        fontSize =
                            12.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                DeviceStatusBadge(
                    status =
                        device.status
                )
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            DeviceInfoRow(
                label =
                    "Customer",

                value =
                    device.customer
            )


            DeviceInfoRow(
                label =
                    "Submitted",

                value =
                    device.date
            )


            DeviceInfoRow(
                label =
                    "Recovery Value",

                value =
                    device.recoveryValue
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            Text(
                text =
                    "Device ID: ${device.id}",

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
// DEVICE ICON
// ============================================================

@Composable
fun DeviceIcon(
    category: String
) {

    val icon =

        when {

            category.contains(
                "Smartphone",
                ignoreCase = true
            ) ->

                Icons.Default.Smartphone


            category.contains(
                "Laptop",
                ignoreCase = true
            ) ->

                Icons.Default.Laptop


            else ->

                Icons.Default.Devices
        }


    Icon(

        imageVector =
            icon,

        contentDescription =
            null,

        tint =
            MaterialTheme
                .colorScheme
                .onPrimaryContainer,

        modifier =
            Modifier.size(26.dp)
    )
}


// ============================================================
// INFO ROW
// ============================================================

@Composable
fun DeviceInfoRow(

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
// STATUS BADGE
// ============================================================

@Composable
fun DeviceStatusBadge(
    status: String
) {

    val background =

        when (status) {

            "Approved",
            "Completed" ->

                MaterialTheme
                    .colorScheme
                    .primaryContainer


            "Pending" ->

                MaterialTheme
                    .colorScheme
                    .secondaryContainer


            "Rejected" ->

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

            "Approved",
            "Completed" ->

                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer


            "Pending" ->

                MaterialTheme
                    .colorScheme
                    .onSecondaryContainer


            "Rejected" ->

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