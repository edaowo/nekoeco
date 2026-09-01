package com.example.nekoeco.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nekoeco.ai.AiScanResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    var selectedDate by rememberSaveable {
        mutableStateOf("")
    }

    var selectedTime by rememberSaveable {
        mutableStateOf("")
    }

    var address by rememberSaveable {
        mutableStateOf("")
    }

    var showDatePicker by rememberSaveable {
        mutableStateOf(false)
    }

    var showTimePicker by rememberSaveable {
        mutableStateOf(false)
    }


    // ========================================================
    // PICKER STATE
    //
    // Held above the if-blocks so that on configuration
    // changes (rotation, dark/light theme switch) the
    // pickers' internal scroll positions survive — these
    // state holders are rememberSaveable under the hood
    // (Material3 1.4.0), so the selection persists even
    // if the activity isn't protected by configChanges.
    // ========================================================

    val datePickerState =
        rememberDatePickerState(
            selectableDates = FutureOnlySelectableDates
        )

    val timePickerState =
        rememberTimePickerState(
            initialHour = BUSINESS_OPEN_HOUR,
            is24Hour = true
        )


    // ========================================================
    // VALIDATION
    //
    // canConfirm + confirmError together replace the old
    // "are the strings non-empty?" check. The button stays
    // disabled and the reason is shown below the address
    // field until every rule passes.
    // ========================================================

    val validation =
        validateBooking(
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            address = address,
            existingAppointments =
                CustomerDataStore
                    .getCurrentCustomerAppointments()
        )

    val canConfirm = validation == null
    val confirmError = validation


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

            // ================================================
            // Disabled until validation passes. The reason is
            // surfaced in confirmError below so the user knows
            // why Confirm isn't greyed out.
            // ================================================

            enabled = canConfirm

        ) {

            Text(
                text = "Confirm Appointment"
            )
        }


        // ====================================================
        // VALIDATION MESSAGE
        //
        // Always rendered so the layout doesn't jump when the
        // reason appears; the text is just blank when valid.
        // ====================================================

        if (
            confirmError != null
        ) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(

                text = confirmError,

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                fontSize = 12.sp,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 4.dp
                    )
            )
        }
    }


    // ========================================================
    // DATE PICKER
    //
    // Replaced the built-in DatePickerDialog with a manual
    // Dialog so the body can be verticalScroll()-wrapped —
    // DatePickerDialog's own layout doesn't scroll, so in
    // landscape (or short screens) the month grid overflowed
    // and looked frozen. usePlatformDefaultWidth = false
    // also lets the dialog use its natural width instead
    // of the AlertDialog 280dp cap.
    // ========================================================

    if (showDatePicker) {

        val dialogScrollState = rememberScrollState()


        Dialog(

            onDismissRequest = {
                showDatePicker = false
            },

            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )

        ) {

            Surface(

                shape = RoundedCornerShape(20.dp),

                color =
                    MaterialTheme
                        .colorScheme
                        .surface,

                tonalElevation = 6.dp

            ) {

                Column(

                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .heightIn(max = 640.dp)
                        .verticalScroll(dialogScrollState)
                        .padding(20.dp)

                ) {

                    DatePicker(
                        state = datePickerState
                    )


                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),

                        horizontalArrangement =
                            Arrangement.End

                    ) {

                        TextButton(
                            onClick = {
                                showDatePicker = false
                            }
                        ) {

                            Text("Cancel")
                        }


                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )


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
                    }
                }
            }
        }
    }


    // ========================================================
// TIME PICKER
//
// Same recipe as the date picker: replaced AlertDialog with
// a manual Dialog so the body can be verticalScroll()-wrapped
// and the picker can size to its natural width (TimePicker's
// hour/minute wheels get clipped inside AlertDialog's 280dp
// cap).
// ========================================================

    if (showTimePicker) {

        val dialogScrollState = rememberScrollState()


        Dialog(

            onDismissRequest = {
                showTimePicker = false
            },

            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )

        ) {

            Surface(

                shape = RoundedCornerShape(20.dp),

                color =
                    MaterialTheme
                        .colorScheme
                        .surface,

                tonalElevation = 6.dp

            ) {

                Column(

                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .verticalScroll(dialogScrollState)
                        .padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally

                ) {

                    TimePicker(
                        state = timePickerState
                    )


                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),

                        horizontalArrangement =
                            Arrangement.End

                    ) {

                        TextButton(
                            onClick = {
                                showTimePicker = false
                            }
                        ) {

                            Text("Cancel")
                        }


                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )


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
                    }
                }
            }
        }
    }
}


// ============================================================
// BUSINESS HOURS
//
// Adjust here if the operating window changes — every check
// below reads from these constants.
// ============================================================

private const val BUSINESS_OPEN_HOUR = 8
private const val BUSINESS_CLOSE_HOUR = 20


// ============================================================
// FUTURE-ONLY DATE SELECTION
//
// DatePickerState takes a SelectableDates whose isSelectableDate
// is checked by the calendar UI before allowing a tap. Without
// this, the picker would happily accept yesterday's date; the
// "future only" rule would then have to be enforced separately
// on Confirm, which is a worse UX because the picker still
// shows past dates as available.
// ============================================================

private object FutureOnlySelectableDates : SelectableDates {

    override fun isSelectableDate(
        utcTimeMillis: Long
    ): Boolean {

        val startOfToday =
            Calendar.getInstance().apply {

                timeZone =
                    TimeZone.getDefault()

                set(
                    Calendar.HOUR_OF_DAY,
                    0
                )

                set(
                    Calendar.MINUTE,
                    0
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )
            }.timeInMillis


        return utcTimeMillis >= startOfToday
    }
}


// ============================================================
// BOOKING VALIDATION
//
// Returns null when the booking is OK, otherwise a short
// message explaining why Confirm is disabled. The booking
// screen binds Confirm's `enabled` to (validation == null).
// ============================================================

private fun validateBooking(
    selectedDate: String,
    selectedTime: String,
    address: String,
    existingAppointments: List<CustomerAppointment>
): String? {

    if (
        selectedDate.isEmpty() ||
        selectedTime.isEmpty() ||
        address.isBlank()
    ) {

        return "Fill in date, time and address to continue."
    }


    // ----- parse selectedDate / selectedTime -----

    val parsedDateMillis =
        parseBookingDateMillis(selectedDate)

    if (parsedDateMillis == null) {

        return "Selected date is invalid."
    }


    val now = Calendar.getInstance()


    val selectedCalendar =
        Calendar.getInstance().apply {

            timeInMillis = parsedDateMillis

            set(
                Calendar.HOUR_OF_DAY,
                0
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )
        }


    // ----- past-date guard -----

    val startOfToday =
        Calendar.getInstance().apply {

            set(
                Calendar.HOUR_OF_DAY,
                0
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )
        }


    if (
        selectedCalendar.timeInMillis <
                startOfToday.timeInMillis
    ) {

        return "Pickup date can't be in the past."
    }


    // ----- parse hour / minute -----

    val parts =
        selectedTime.split(":")

    if (parts.size != 2) {

        return "Selected time is invalid."
    }


    val hour =
        parts[0].toIntOrNull()
            ?: return "Selected time is invalid."

    val minute =
        parts[1].toIntOrNull()
            ?: return "Selected time is invalid."


    // ----- business hours -----

    if (
        hour < BUSINESS_OPEN_HOUR ||
        hour >= BUSINESS_CLOSE_HOUR
    ) {

        return "Pickup time must be between " +
                "%02d:00 and %02d:00.".format(
                    BUSINESS_OPEN_HOUR,
                    BUSINESS_CLOSE_HOUR
                )
    }


    // ----- past-time guard for today -----

    if (
        selectedCalendar.timeInMillis ==
                startOfToday.timeInMillis
    ) {

        if (
            hour < now.get(Calendar.HOUR_OF_DAY) ||
            (
                hour == now.get(
                    Calendar.HOUR_OF_DAY
                ) &&
                minute <= now.get(
                    Calendar.MINUTE
                )
            )
        ) {

            return "Pickup time can't be in the past."
        }
    }


    // ----- duplicate slot guard -----

    val duplicate =
        existingAppointments.any {

            it.date == selectedDate &&
            it.time == selectedTime &&
            it.status != "Cancelled"
        }


    if (duplicate) {

        return "You already have a pickup booked at this time."
    }


    return null
}


// ============================================================
// PARSE BOOKING DATE
//
// selectedDate is stored as "dd MMM yyyy" (e.g. "01 Sep 2026")
// by the date-picker dialog. Parses it back to midnight
// millis in the device's local time zone so we can compare
// it to "today".
// ============================================================

private fun parseBookingDateMillis(
    label: String
): Long? {

    return try {

        val parser =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )

        parser.isLenient = false

        parser.parse(label)?.time

    } catch (t: Throwable) {

        null
    }
}

