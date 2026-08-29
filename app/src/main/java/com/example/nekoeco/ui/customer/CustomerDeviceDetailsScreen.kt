package com.example.nekoeco.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

data class CustomerDeviceDetailsUiState(

    val device: CustomerDevice? = null,

    val editName: String = "",

    val showEditDialog: Boolean = false,

    val showDeleteDialog: Boolean = false,

    val errorMessage: String = "",

    val deviceDeleted: Boolean = false

) {

    val canDelete: Boolean
        get() =
            device?.status == "Pending" ||
                    device?.status == "Rejected"


    val canEditName: Boolean
        get() =
            device != null &&
                    device.status != "Completed"
}


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerDeviceDetailsEvent {

    data class LoadDevice(
        val device: CustomerDevice
    ) : CustomerDeviceDetailsEvent


    data object EditClicked :
        CustomerDeviceDetailsEvent


    data class NameChanged(
        val name: String
    ) : CustomerDeviceDetailsEvent


    data object SaveName :
        CustomerDeviceDetailsEvent


    data object DismissEdit :
        CustomerDeviceDetailsEvent


    data object DeleteClicked :
        CustomerDeviceDetailsEvent


    data object ConfirmDelete :
        CustomerDeviceDetailsEvent


    data object DismissDelete :
        CustomerDeviceDetailsEvent


    data object DeleteHandled :
        CustomerDeviceDetailsEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerDeviceDetailsViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerDeviceDetailsUiState()
        )


    val uiState: StateFlow<CustomerDeviceDetailsUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: CustomerDeviceDetailsEvent
    ) {

        when (event) {

            is CustomerDeviceDetailsEvent.LoadDevice -> {

                loadDevice(
                    event.device
                )
            }


            CustomerDeviceDetailsEvent.EditClicked -> {

                val device =
                    _uiState.value.device
                        ?: return


                _uiState.update {

                    it.copy(
                        editName = device.deviceName,
                        showEditDialog = true,
                        errorMessage = ""
                    )
                }
            }


            is CustomerDeviceDetailsEvent.NameChanged -> {

                _uiState.update {

                    it.copy(
                        editName = event.name,
                        errorMessage = ""
                    )
                }
            }


            CustomerDeviceDetailsEvent.SaveName -> {

                saveName()
            }


            CustomerDeviceDetailsEvent.DismissEdit -> {

                _uiState.update {

                    it.copy(
                        showEditDialog = false,
                        errorMessage = ""
                    )
                }
            }


            CustomerDeviceDetailsEvent.DeleteClicked -> {

                _uiState.update {

                    it.copy(
                        showDeleteDialog = true
                    )
                }
            }


            CustomerDeviceDetailsEvent.ConfirmDelete -> {

                deleteDevice()
            }


            CustomerDeviceDetailsEvent.DismissDelete -> {

                _uiState.update {

                    it.copy(
                        showDeleteDialog = false
                    )
                }
            }


            CustomerDeviceDetailsEvent.DeleteHandled -> {

                _uiState.update {

                    it.copy(
                        deviceDeleted = false
                    )
                }
            }
        }
    }


    // ========================================================
    // LOAD DEVICE
    // ========================================================

    private fun loadDevice(
        originalDevice: CustomerDevice
    ) {

        val latestDevice =
            CustomerDataStore.devices.find {

                it.deviceId ==
                        originalDevice.deviceId
            }


        _uiState.update {

            it.copy(
                device =
                    latestDevice
                        ?: originalDevice,

                editName =
                    latestDevice
                        ?.deviceName
                        ?: originalDevice.deviceName,

                showEditDialog = false,
                showDeleteDialog = false,
                errorMessage = "",
                deviceDeleted = false
            )
        }
    }


    // ========================================================
    // SAVE NAME
    // ========================================================

    private fun saveName() {

        val device =
            _uiState.value.device
                ?: return


        if (
            device.status ==
            "Completed"
        ) {

            return
        }


        val newName =
            _uiState.value
                .editName
                .trim()


        if (
            newName.isBlank()
        ) {

            _uiState.update {

                it.copy(
                    errorMessage =
                        "Please enter a device name."
                )
            }

            return
        }


        CustomerDataStore.updateDeviceName(
            deviceId = device.deviceId,
            newName = newName
        )


        val updatedDevice =
            CustomerDataStore.devices.find {

                it.deviceId ==
                        device.deviceId
            }


        _uiState.update {

            it.copy(
                device =
                    updatedDevice
                        ?: device.copy(
                            deviceName =
                                newName
                        ),

                editName =
                    newName,

                showEditDialog =
                    false,

                errorMessage =
                    ""
            )
        }
    }


    // ========================================================
    // DELETE DEVICE
    // ========================================================

    private fun deleteDevice() {

        val device =
            _uiState.value.device
                ?: return


        if (
            device.status != "Pending" &&
            device.status != "Rejected"
        ) {

            return
        }


        CustomerDataStore.deleteDeviceByCustomer(
            device.deviceId
        )


        _uiState.update {

            it.copy(
                device = null,
                showDeleteDialog = false,
                deviceDeleted = true
            )
        }
    }
}


// ============================================================
// SCREEN
// ============================================================

@Composable
fun CustomerDeviceDetailsScreen(

    device: CustomerDevice,

    onBackClick: () -> Unit,

    onDeviceDeleted: () -> Unit,

    viewModel: CustomerDeviceDetailsViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // LOAD LATEST DEVICE
    // ========================================================

    LaunchedEffect(
        device.deviceId
    ) {

        viewModel.onEvent(
            CustomerDeviceDetailsEvent
                .LoadDevice(device)
        )
    }


    // ========================================================
    // DELETE FINISHED
    // ========================================================

    LaunchedEffect(
        uiState.deviceDeleted
    ) {

        if (
            uiState.deviceDeleted
        ) {

            onDeviceDeleted()


            viewModel.onEvent(
                CustomerDeviceDetailsEvent
                    .DeleteHandled
            )
        }
    }


    val currentDevice =
        uiState.device
            ?: return


    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
    ) {


        // ====================================================
        // TOP BAR
        // ====================================================

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
                    .padding(
                        horizontal = 8.dp,
                        vertical = 6.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBackClick
            ) {

                Icon(
                    imageVector =
                        Icons.AutoMirrored
                            .Filled
                            .ArrowBack,

                    contentDescription =
                        "Back",

                    tint =
                        MaterialTheme.colorScheme.onPrimary
                )
            }


            Text(
                text =
                    "Device Details",

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onPrimary
            )
        }


        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp
                    )
        ) {


            // =================================================
            // DEVICE
            // =================================================

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
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text =
                            currentDevice.deviceName,

                        fontSize =
                            22.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )


                    Text(
                        text =
                            currentDevice.category,

                        fontSize =
                            13.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    CustomerDeviceStatus(
                        status =
                            currentDevice.status
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // DEVICE INFO
            // =================================================

            Text(
                text =
                    "Device Information",

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


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
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    CustomerDeviceDetailRow(
                        label =
                            "Device ID",

                        value =
                            currentDevice.deviceId
                    )


                    CustomerDeviceDetailRow(
                        label =
                            "Category",

                        value =
                            currentDevice.category
                    )


                    CustomerDeviceDetailRow(
                        label =
                            "Submitted",

                        value =
                            currentDevice.date
                    )


                    CustomerDeviceDetailRow(
                        label =
                            "Status",

                        value =
                            currentDevice.status
                    )


                    CustomerDeviceDetailRow(
                        label =
                            "Recovery Value",

                        value =
                            "RM %.2f".format(
                                currentDevice.recoveryValue
                            )
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // PARTS ANALYSIS
            // =================================================

            Text(
                text =
                    "Parts Analysis",

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


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
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    if (
                        currentDevice
                            .accessories
                            .isEmpty()
                    ) {

                        Text(
                            text =
                                "No parts information available.",

                            fontSize =
                                13.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                    } else {

                        currentDevice
                            .accessories
                            .forEach { accessory ->

                                CustomerDeviceDetailRow(

                                    label =
                                        accessory.name,

                                    value =
                                        "RM %.2f".format(
                                            accessory.price
                                        )
                                )
                            }
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )


            // =================================================
            // EDIT NAME
            // =================================================

            if (
                uiState.canEditName
            ) {

                OutlinedButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerDeviceDetailsEvent
                                .EditClicked
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(14.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Edit,

                        contentDescription =
                            null
                    )


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Text(
                        text =
                            "Edit Device Name"
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }


            // =================================================
            // DELETE
            // =================================================

            if (
                uiState.canDelete
            ) {

                OutlinedButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerDeviceDetailsEvent
                                .DeleteClicked
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(14.dp),

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
                            Icons.Default.Delete,

                        contentDescription =
                            "Delete"
                    )


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Text(
                        text =
                            "Delete Device"
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )
        }
    }


    // ========================================================
    // EDIT DIALOG
    // ========================================================

    if (
        uiState.showEditDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                viewModel.onEvent(
                    CustomerDeviceDetailsEvent
                        .DismissEdit
                )
            },

            title = {

                Text(
                    text =
                        "Edit Device Name"
                )
            },

            text = {

                Column {

                    OutlinedTextField(

                        value =
                            uiState.editName,

                        onValueChange = {

                            viewModel.onEvent(
                                CustomerDeviceDetailsEvent
                                    .NameChanged(it)
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "Device Name"
                            )
                        },

                        singleLine =
                            true,

                        isError =
                            uiState.errorMessage
                                .isNotBlank()
                    )


                    if (
                        uiState.errorMessage
                            .isNotBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )


                        Text(
                            text =
                                uiState.errorMessage,

                            fontSize =
                                12.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerDeviceDetailsEvent
                                .SaveName
                        )
                    }

                ) {

                    Text(
                        text =
                            "Save"
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerDeviceDetailsEvent
                                .DismissEdit
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


    // ========================================================
    // DELETE DIALOG
    // ========================================================

    if (
        uiState.showDeleteDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                viewModel.onEvent(
                    CustomerDeviceDetailsEvent
                        .DismissDelete
                )
            },

            title = {

                Text(
                    text =
                        "Delete Device?"
                )
            },

            text = {

                Text(
                    text =
                        "Are you sure you want to delete ${currentDevice.deviceName}? This action cannot be undone."
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerDeviceDetailsEvent
                                .ConfirmDelete
                        )
                    }

                ) {

                    Text(
                        text =
                            "Delete",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerDeviceDetailsEvent
                                .DismissDelete
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
// DETAIL ROW
// ============================================================

@Composable
private fun CustomerDeviceDetailRow(

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