package com.example.nekoeco.ui.staff

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// ============================================================
// STAFF DEVICE DETAILS
// ============================================================

@Composable
fun StaffDeviceDetailsScreen(

    device: StaffDevice,

    currentStatus: String,

    showDeleteDialog: Boolean,

    // Component names currently ticked in the
    // inspection checkboxes (owned by the ViewModel).
    inspectionSelection: Set<String>,

    onEvent: (StaffDevicesEvent) -> Unit

) {

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
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(

                onClick = {

                    onEvent(
                        StaffDevicesEvent
                            .BackFromDetails
                    )
                }

            ) {

                Icon(
                    imageVector =
                        Icons.AutoMirrored
                            .Filled
                            .ArrowBack,

                    contentDescription =
                        "Back"
                )
            }


            Text(
                text =
                    "Device Details",

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Bold
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
            // DEVICE HEADER
            // =================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

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
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Devices,

                        contentDescription =
                            "Device",

                        modifier =
                            Modifier.size(65.dp),

                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    Text(
                        text =
                            device.deviceName,

                        fontSize =
                            23.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    Text(
                        text =
                            device.category,

                        fontSize =
                            14.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    DeviceStatusBadge(
                        status =
                            currentStatus
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // CUSTOMER INFORMATION
            // =================================================

            SectionTitle(
                title =
                    "Customer Information"
            )


            InformationCard {

                DetailRow(
                    label =
                        "Customer",

                    value =
                        device.customer
                )


                DetailRow(
                    label =
                        "Customer ID",

                    value =
                        "C001"
                )


                DetailRow(
                    label =
                        "Device ID",

                    value =
                        device.id
                )


                DetailRow(
                    label =
                        "Submitted",

                    value =
                        device.date
                )
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // DEVICE INFORMATION
            // =================================================

            SectionTitle(
                title =
                    "Device Information"
            )


            InformationCard {

                DetailRow(
                    label =
                        "Device Type",

                    value =
                        device.category
                )


                DetailRow(
                    label =
                        "Condition",

                    value =
                        "Good"
                )


                DetailRow(
                    label =
                        "Estimated Value",

                    value =
                        device.recoveryValue
                )
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // AI ANALYSIS
            // =================================================

            SectionTitle(
                title =
                    "AI Analysis"
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
                                .primaryContainer
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Security,

                            contentDescription =
                                "AI Analysis",

                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimaryContainer
                        )


                        Spacer(
                            modifier =
                                Modifier.size(10.dp)
                        )


                        Text(
                            text =
                                "AI Parts Analysis",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimaryContainer
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(15.dp)
                    )


                    DetailRow(
                        label =
                            "Recyclability",

                        value =
                            "86%"
                    )


                    DetailRow(
                        label =
                            "Recovery Value",

                        value =
                            device.recoveryValue
                    )


                    DetailRow(
                        label =
                            "AI Confidence",

                        value =
                            "94%"
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // DEVICE INSPECTION
            // =================================================

            SectionTitle(
                title =
                    "Device Inspection"
            )


            InformationCard {

                if (
                    device.components.isEmpty()
                ) {

                    Text(
                        text =
                            "No component inspection data available.",

                        fontSize =
                            13.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                } else {

                    // =========================================
                    // SELECT ALL
                    // =========================================

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked =

                                inspectionSelection.size ==
                                        device.components.size,

                            onCheckedChange = { checked ->

                                onEvent(
                                    StaffDevicesEvent
                                        .InspectionSelectAll(
                                            selected =
                                                checked
                                        )
                                )
                            }
                        )


                        Text(
                            text =
                                "Select all parts",

                            fontSize =
                                13.sp,

                            fontWeight =
                                FontWeight.Bold,

                            modifier =
                                Modifier.weight(1f)
                        )
                    }


                    device.components
                        .forEach { component ->

                            StaffInspectionComponentRow(
                                component =
                                    component,

                                approved =

                                    inspectionSelection.contains(
                                        component.name
                                    ),

                                onToggle = {

                                    onEvent(
                                        StaffDevicesEvent
                                            .InspectionToggled(
                                                componentName =
                                                    component.name
                                            )
                                    )
                                }
                            )
                        }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // INSPECTION SUMMARY
            // =================================================

            SectionTitle(
                title =
                    "Inspection Summary"
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

                    val sellableCount =
                        device.components.count {

                            it.status ==
                                    "Sellable"
                        }


                    val recyclableCount =
                        device.components.count {

                            it.status ==
                                    "Recyclable"
                        }


                    val notSellableCount =
                        device.components.count {

                            it.status ==
                                    "Not Sellable"
                        }


                    DetailRow(
                        label =
                            "Total Components",

                        value =
                            device
                                .components
                                .size
                                .toString()
                    )


                    DetailRow(
                        label =
                            "Sellable",

                        value =
                            sellableCount
                                .toString()
                    )


                    DetailRow(
                        label =
                            "Recyclable",

                        value =
                            recyclableCount
                                .toString()
                    )


                    DetailRow(
                        label =
                            "Not Sellable",

                        value =
                            notSellableCount
                                .toString()
                    )


                    // =========================================
                    // LIVE APPROVED TOTAL
                    //
                    // What the company will actually pay
                    // for, based on the current checkbox
                    // selection — saved to the device when
                    // the staff approves it.
                    // =========================================

                    val approvedTotal =
                        device.components
                            .filter {

                                inspectionSelection.contains(
                                    it.name
                                )
                            }
                            .sumOf { component ->

                                component.price
                                    .replace(
                                        Regex("[^0-9.]"),
                                        ""
                                    )
                                    .toDoubleOrNull()
                                    ?: 0.0
                            }


                    DetailRow(
                        label =
                            "Approved Total",

                        value =
                            "RM %.2f".format(
                                approvedTotal
                            )
                    )


                    DetailRow(
                        label =
                            "Recovery Value",

                        value =
                            device.recoveryValue
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // =================================================
            // APPROVE
            // =================================================

            if (
                currentStatus == "Pending" ||
                currentStatus == "Rejected"
            ) {

                Button(

                    onClick = {

                        onEvent(
                            StaffDevicesEvent
                                .StatusChanged(
                                    "Approved"
                                )
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(14.dp),

                    // Approving with zero parts ticked
                    // would save an RM 0 device — require
                    // at least one part, or a rejection.

                    enabled =

                        device.components.isEmpty() ||
                                inspectionSelection.isNotEmpty()
                ) {

                    Text(
                        text =
                            "✓  Approve Device",

                        fontSize =
                            15.sp
                    )
                }


                if (
                    device.components.isNotEmpty() &&
                    inspectionSelection.isEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    Text(
                        text =
                            "Tick at least one part to approve — otherwise use Reject.",

                        fontSize =
                            11.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }


            // =================================================
            // SET TO PENDING
            //
            // Devices start Pending automatically at booking;
            // this reverts an Approved/Rejected device back to
            // Pending (updateDeviceStatus cancels its non-
            // completed appointment and resets the payment).
            // =================================================

            if (
                currentStatus == "Approved" ||
                currentStatus == "Rejected"
            ) {

                TextButton(

                    onClick = {

                        onEvent(
                            StaffDevicesEvent
                                .StatusChanged(
                                    "Pending"
                                )
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Refresh,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(18.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Text(
                        text =
                            "Set to Pending",

                        fontSize =
                            13.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }


            // =================================================
            // REJECT
            // =================================================

            if (
                currentStatus != "Rejected" &&
                currentStatus != "Completed"
            ) {

                OutlinedButton(

                    onClick = {

                        onEvent(
                            StaffDevicesEvent
                                .StatusChanged(
                                    "Rejected"
                                )
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

                    Text(
                        text =
                            "✕  Reject Device",

                        fontSize =
                            15.sp
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }


            // =================================================
            // COMPLETED
            // =================================================

            if (
                currentStatus ==
                "Completed"
            ) {

                Text(
                    text =
                        "✓ This device has been completed through the appointment.",

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }


            // =================================================
            // DELETE
            // =================================================

            OutlinedButton(

                onClick = {

                    onEvent(
                        StaffDevicesEvent
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
                        Modifier.size(8.dp)
                )


                Text(
                    text =
                        "Delete Device"
                )
            }


            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )
        }
    }


    // ========================================================
    // DELETE DIALOG
    // ========================================================

    if (
        showDeleteDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                onEvent(
                    StaffDevicesEvent
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
                        "Are you sure you want to delete ${device.deviceName}? This action cannot be undone."
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        onEvent(
                            StaffDevicesEvent
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

                        onEvent(
                            StaffDevicesEvent
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
// INSPECTION COMPONENT ROW
// ============================================================

@Composable
fun StaffInspectionComponentRow(

    component: StaffComponent,

    // Whether the company will recover this part —
    // rendered as the row's checkbox.
    approved: Boolean,

    onToggle: () -> Unit

) {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 10.dp
                )
    ) {

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Checkbox(
                checked =
                    approved,

                onCheckedChange = {

                    onToggle()
                }
            )


            Icon(
                imageVector =
                    Icons.Default.Devices,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(22.dp),

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )


            Spacer(
                modifier =
                    Modifier.size(10.dp)
            )


            Text(
                text =
                    component.name,

                modifier =
                    Modifier.weight(1f),

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Text(
                text =
                    component.status,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    getComponentStatusColor(
                        component.status
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )


        DetailRow(
            label =
                "Condition",

            value =
                component.condition
        )


        DetailRow(
            label =
                "Price",

            value =
                component.price
        )


        Spacer(
            modifier =
                Modifier.height(4.dp)
        )
    }
}


// ============================================================
// COMPONENT STATUS COLOR
// ============================================================

@Composable
fun getComponentStatusColor(
    status: String
) =

    when (status) {

        "Sellable" ->

            MaterialTheme
                .colorScheme
                .primary


        "Recyclable" ->

            MaterialTheme
                .colorScheme
                .secondary


        "Not Sellable" ->

            MaterialTheme
                .colorScheme
                .error


        else ->

            MaterialTheme
                .colorScheme
                .onSurfaceVariant
    }


// ============================================================
// SECTION TITLE
// ============================================================

@Composable
fun SectionTitle(
    title: String
) {

    Text(
        text =
            title,

        fontSize =
            18.sp,

        fontWeight =
            FontWeight.Bold
    )


    Spacer(
        modifier =
            Modifier.height(8.dp)
    )
}


// ============================================================
// INFORMATION CARD
// ============================================================

@Composable
fun InformationCard(

    content: @Composable () -> Unit

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
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            content()
        }
    }
}


// ============================================================
// DETAIL ROW
// ============================================================

@Composable
fun DetailRow(

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
                13.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Text(
            text =
                value,

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.Medium
        )
    }
}