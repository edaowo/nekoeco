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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

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

data class CustomerDevicesUiState(

    val searchText: String = "",

    val selectedFilter: String = "All",

    val devices: List<CustomerDevice> = emptyList()

) {

    val filteredDevices: List<CustomerDevice>
        get() =
            devices.filter { device ->

                val searchMatch =

                    device.deviceName.contains(
                        searchText,
                        ignoreCase = true
                    ) ||

                            device.category.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            device.deviceId.contains(
                                searchText,
                                ignoreCase = true
                            )


                val statusMatch =

                    selectedFilter == "All" ||
                            device.status == selectedFilter


                searchMatch && statusMatch
            }
}


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerDevicesEvent {

    data class SearchChanged(
        val text: String
    ) : CustomerDevicesEvent


    data class FilterChanged(
        val filter: String
    ) : CustomerDevicesEvent


    data object RefreshDevices :
        CustomerDevicesEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerDevicesViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerDevicesUiState(
                devices =
                    CustomerDataStore
                        .getCurrentCustomerDevices()
            )
        )


    val uiState: StateFlow<CustomerDevicesUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: CustomerDevicesEvent
    ) {

        when (event) {

            is CustomerDevicesEvent.SearchChanged -> {

                _uiState.update {

                    it.copy(
                        searchText = event.text
                    )
                }
            }


            is CustomerDevicesEvent.FilterChanged -> {

                _uiState.update {

                    it.copy(
                        selectedFilter = event.filter
                    )
                }
            }


            CustomerDevicesEvent.RefreshDevices -> {

                refreshDevices()
            }
        }
    }


    private fun refreshDevices() {

        _uiState.update {

            it.copy(
                devices =
                    CustomerDataStore
                        .getCurrentCustomerDevices()
            )
        }
    }
}


// ============================================================
// CUSTOMER DEVICES SCREEN
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerDevicesScreen(

    onAddDevice: () -> Unit,

    onDeviceClick: (CustomerDevice) -> Unit,

    viewModel: CustomerDevicesViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {

        viewModel.onEvent(
            CustomerDevicesEvent.RefreshDevices
        )
    }


    val filteredDevices =
        uiState.filteredDevices


    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(
                horizontal = 20.dp
            )
    ) {

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Text(
            text = "Devices",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme.colorScheme.onBackground
        )


        Text(
            text = "Manage your recycling devices",
            fontSize = 13.sp,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )


        Spacer(
            modifier = Modifier.height(14.dp)
        )


        Button(

            onClick = onAddDevice,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape =
                RoundedCornerShape(14.dp)

        ) {

            Icon(
                imageVector =
                    Icons.Default.CameraAlt,
                contentDescription =
                    "AI Scan"
            )


            Spacer(
                modifier = Modifier.width(8.dp)
            )


            Text(
                text = "AI Scan Device",
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        OutlinedTextField(

            value = uiState.searchText,

            onValueChange = {

                viewModel.onEvent(
                    CustomerDevicesEvent
                        .SearchChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            shape =
                RoundedCornerShape(14.dp),

            placeholder = {
                Text(
                    text = "Search devices"
                )
            },

            leadingIcon = {

                Icon(
                    imageVector =
                        Icons.Default.Search,
                    contentDescription =
                        "Search"
                )
            }
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        FlowRow(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(6.dp),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)

        ) {

            CustomerDeviceFilter(
                text = "All",
                selected =
                    uiState.selectedFilter == "All"
            ) {

                viewModel.onEvent(
                    CustomerDevicesEvent
                        .FilterChanged("All")
                )
            }


            CustomerDeviceFilter(
                text = "Pending",
                selected =
                    uiState.selectedFilter == "Pending"
            ) {

                viewModel.onEvent(
                    CustomerDevicesEvent
                        .FilterChanged("Pending")
                )
            }


            CustomerDeviceFilter(
                text = "Approved",
                selected =
                    uiState.selectedFilter == "Approved"
            ) {

                viewModel.onEvent(
                    CustomerDevicesEvent
                        .FilterChanged("Approved")
                )
            }


            CustomerDeviceFilter(
                text = "Scheduled",
                selected =
                    uiState.selectedFilter == "Scheduled"
            ) {

                viewModel.onEvent(
                    CustomerDevicesEvent
                        .FilterChanged("Scheduled")
                )
            }


            CustomerDeviceFilter(
                text = "Rejected",
                selected =
                    uiState.selectedFilter == "Rejected"
            ) {

                viewModel.onEvent(
                    CustomerDevicesEvent
                        .FilterChanged("Rejected")
                )
            }


            CustomerDeviceFilter(
                text = "Completed",
                selected =
                    uiState.selectedFilter == "Completed"
            ) {

                viewModel.onEvent(
                    CustomerDevicesEvent
                        .FilterChanged("Completed")
                )
            }
        }


        Spacer(
            modifier = Modifier.height(14.dp)
        )


        Text(
            text =
                "${filteredDevices.size} device(s)",

            fontSize = 13.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        if (filteredDevices.isEmpty()) {

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
                            Icons.Default.Devices,

                        contentDescription = null,

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
                            "No devices found",

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
                            "Your recycling devices will appear here.",

                        fontSize = 12.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

        } else {

            LazyColumn(

                modifier =
                    Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                items(

                    items =
                        filteredDevices,

                    key = {
                        it.deviceId
                    }

                ) { device ->

                    CustomerDeviceCard(

                        device =
                            device,

                        onClick = {

                            onDeviceClick(
                                device
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
}


// ============================================================
// FILTER
// ============================================================

@Composable
fun CustomerDeviceFilter(

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
// DEVICE CARD
// ============================================================

@Composable
fun CustomerDeviceCard(

    device: CustomerDevice,

    onClick: () -> Unit

) {

    Card(

        modifier = Modifier
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
                defaultElevation = 3.dp
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

                    modifier = Modifier
                        .size(52.dp)
                        .clip(
                            RoundedCornerShape(15.dp)
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
                            Icons.Default.Devices,

                        contentDescription = null,

                        modifier =
                            Modifier.size(28.dp),

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
                            device.deviceName,

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(
                        text =
                            device.category,

                        fontSize = 12.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                CustomerDeviceStatus(
                    status =
                        device.status
                )
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            CustomerDeviceInfoRow(
                label = "Submitted",
                value = device.date
            )


            CustomerDeviceInfoRow(
                label = "Recovery Value",
                value =
                    "RM %.2f".format(
                        device.recoveryValue
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            Text(
                text =
                    "Device ID: ${device.deviceId}",

                fontSize = 10.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// ============================================================
// DEVICE INFO
// ============================================================

@Composable
private fun CustomerDeviceInfoRow(

    label: String,

    value: String

) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween

    ) {

        Text(
            text = label,
            fontSize = 12.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }


    Spacer(
        modifier =
            Modifier.height(4.dp)
    )
}


// ============================================================
// STATUS
// ============================================================

@Composable
fun CustomerDeviceStatus(

    status: String

) {

    val containerColor =

        when (status) {

            "Approved" ->
                MaterialTheme
                    .colorScheme
                    .primaryContainer

            "Scheduled" ->
                MaterialTheme
                    .colorScheme
                    .secondaryContainer

            "Completed" ->
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer

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

            "Approved" ->
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer

            "Scheduled" ->
                MaterialTheme
                    .colorScheme
                    .onSecondaryContainer

            "Completed" ->
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer

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

        text = status,

        modifier =
            Modifier
                .background(
                    color =
                        containerColor,

                    shape =
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