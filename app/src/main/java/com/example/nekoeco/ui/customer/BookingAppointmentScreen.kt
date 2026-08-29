package com.example.nekoeco.ui.customer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.nekoeco.ai.AiScanResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingAppointmentScreen(
    result: AiScanResponse,
    onBack: () -> Unit,
    onConfirm: (
        date: String,
        time: String,
        address: String
    ) -> Unit
) {

    // ========================================================
    // STATE
    // ========================================================

    var selectedDate by remember {
        mutableStateOf("")
    }

    var selectedTime by remember {
        mutableStateOf("")
    }

    var address by remember {
        mutableStateOf("")
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }


    // ========================================================
    // SCREEN
    // ========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // ====================================================
        // BACK
        // ====================================================

        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }


        Text(
            text = "Book Pickup",
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ====================================================
        // DEVICE
        // ====================================================

        Text(
            text = "Device",
            style = MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = result.device.device,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Detected from AI Scan",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ====================================================
        // PICKUP DATE
        // ====================================================

        Text(
            text = "Pickup Date",
            style = MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        OutlinedButton(
            onClick = {
                showDatePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Select Date"
            )

            Spacer(
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = if (selectedDate.isEmpty()) {
                    "Select Date"
                } else {
                    selectedDate
                }
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ====================================================
        // PICKUP TIME
        // ====================================================

        Text(
            text = "Pickup Time",
            style = MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        OutlinedButton(
            onClick = {
                showTimePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Select Time"
            )

            Spacer(
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = if (selectedTime.isEmpty()) {
                    "Select Time"
                } else {
                    selectedTime
                }
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ====================================================
        // ADDRESS
        // ====================================================

        Text(
            text = "Pickup Address",
            style = MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        OutlinedTextField(
            value = address,

            onValueChange = {
                address = it
            },

            modifier = Modifier.fillMaxWidth(),

            placeholder = {
                Text(
                    "Enter pickup address"
                )
            },

            singleLine = false,

            minLines = 3
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // ====================================================
        // CONFIRM APPOINTMENT
        // ====================================================

        Button(
            onClick = {

                onConfirm(
                    selectedDate,
                    selectedTime,
                    address
                )
            },

            modifier = Modifier.fillMaxWidth(),

            enabled =
                selectedDate.isNotEmpty() &&
                        selectedTime.isNotEmpty() &&
                        address.isNotBlank()
        ) {

            Text(
                text = "Confirm Appointment"
            )
        }
    }


    // ========================================================
    // DATE PICKER
    // ========================================================

    if (showDatePicker) {

        val datePickerState =
            androidx.compose.material3.rememberDatePickerState()


        DatePickerDialog(

            onDismissRequest = {
                showDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val selectedMillis =
                            datePickerState.selectedDateMillis

                        if (selectedMillis != null) {

                            selectedDate =
                                SimpleDateFormat(
                                    "dd MMM yyyy",
                                    Locale.getDefault()
                                ).format(
                                    Date(selectedMillis)
                                )
                        }

                        showDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }


    // ========================================================
// TIME PICKER
// ========================================================

    if (showTimePicker) {

        val timePickerState =
            androidx.compose.material3.rememberTimePickerState()


        AlertDialog(

            onDismissRequest = {
                showTimePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        selectedTime =
                            String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )

                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            },

            text = {

                TimePicker(
                    state = timePickerState
                )
            }
        )
    }
}

