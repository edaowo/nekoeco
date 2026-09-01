package com.example.nekoeco.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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

data class StaffPaymentsUiState(

    val payments: List<StaffPayment> =
        emptyList(),

    val searchText: String =
        "",

    val selectedFilter: String =
        "All",

    val selectedPayment: StaffPayment? =
        null,

    val selectedDeviceCompleted: Boolean =
        false,

    // Payout method chosen in the collection dialog
    // (Bank Transfer / Touch 'n Go / Cash).
    val collectionMethod: String =
        "Bank Transfer"

) {

    val filteredPayments: List<StaffPayment>
        get() =
            payments.filter { payment ->

                val searchMatch =

                    payment.customer.contains(
                        searchText,
                        ignoreCase = true
                    ) ||

                            payment.device.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            payment.id.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            payment.appointmentId.contains(
                                searchText,
                                ignoreCase = true
                            )


                val statusMatch =

                    selectedFilter == "All" ||
                            payment.status ==
                            selectedFilter


                searchMatch &&
                        statusMatch
            }


    val pendingAmount: Double
        get() =
            payments
                .filter {
                    it.status == "Pending"
                }
                .sumOf {
                    parseAmount(
                        it.amount
                    )
                }


    val paidAmount: Double
        get() =
            payments
                .filter {
                    it.status == "Paid"
                }
                .sumOf {
                    parseAmount(
                        it.amount
                    )
                }
}


// ============================================================
// EVENTS
// ============================================================

sealed interface StaffPaymentsEvent {

    data class SearchChanged(
        val text: String
    ) : StaffPaymentsEvent


    data class FilterChanged(
        val filter: String
    ) : StaffPaymentsEvent


    data class PaymentSelected(
        val payment: StaffPayment
    ) : StaffPaymentsEvent


    data object DismissDetails :
        StaffPaymentsEvent


    data object MarkPaid :
        StaffPaymentsEvent


    data class CollectionMethodChanged(
        val method: String
    ) : StaffPaymentsEvent


    data object RefreshPayments :
        StaffPaymentsEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class StaffPaymentsViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StaffPaymentsUiState(
                payments =
                    StaffDataStore
                        .payments
                        .toList()
            )
        )


    val uiState: StateFlow<StaffPaymentsUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: StaffPaymentsEvent
    ) {

        when (event) {

            is StaffPaymentsEvent.SearchChanged -> {

                _uiState.update {

                    it.copy(
                        searchText =
                            event.text
                    )
                }
            }


            is StaffPaymentsEvent.FilterChanged -> {

                _uiState.update {

                    it.copy(
                        selectedFilter =
                            event.filter
                    )
                }
            }


            is StaffPaymentsEvent.PaymentSelected -> {

                val deviceCompleted =

                    StaffDataStore
                        .devices
                        .any {

                            it.id ==
                                    event.payment.deviceId &&

                                    it.status ==
                                    "Completed"
                        }


                _uiState.update {

                    it.copy(

                        selectedPayment =
                            event.payment,

                        selectedDeviceCompleted =
                            deviceCompleted
                    )
                }
            }


            StaffPaymentsEvent.DismissDetails -> {

                _uiState.update {

                    it.copy(

                        selectedPayment =
                            null,

                        selectedDeviceCompleted =
                            false
                    )
                }
            }


            StaffPaymentsEvent.MarkPaid -> {

                markPaid()
            }


            is StaffPaymentsEvent.CollectionMethodChanged -> {

                _uiState.update {

                    it.copy(
                        collectionMethod =
                            event.method
                    )
                }
            }


            StaffPaymentsEvent.RefreshPayments -> {

                refreshPayments()
            }
        }
    }


    // ========================================================
    // MARK PAID
    // ========================================================

    private fun markPaid() {

        val selectedPayment =

            _uiState.value
                .selectedPayment
                ?: return


        StaffDataStore.markPaymentPaid(
            paymentId =
                selectedPayment.id,

            method =
                _uiState.value.collectionMethod
        )


        refreshPayments()


        _uiState.update {

            it.copy(

                selectedPayment =
                    null,

                selectedDeviceCompleted =
                    false
            )
        }
    }


    // ========================================================
    // REFRESH
    // ========================================================

    private fun refreshPayments() {

        val latestPayments =

            StaffDataStore
                .payments
                .toList()


        val currentSelected =
            _uiState.value
                .selectedPayment


        val latestSelected =

            currentSelected?.let { selected ->

                latestPayments.find {
                    it.id ==
                            selected.id
                }
            }


        val deviceCompleted =

            latestSelected?.let { payment ->

                StaffDataStore
                    .devices
                    .any {

                        it.id ==
                                payment.deviceId &&

                                it.status ==
                                "Completed"
                    }

            } ?: false


        // ====================================================
        // COMPLETION HAND-OFF
        //
        // completeAppointment records the payment to collect
        // in StaffDataStore.lastCollectedPaymentId. On the
        // first refresh after arriving on this tab, open
        // that payment's collection dialog on the Pending
        // filter, then clear the hand-off.
        // ====================================================

        val handOffId =

            StaffDataStore
                .lastCollectedPaymentId

        val handOffPayment =

            handOffId?.let { id ->

                latestPayments.find {
                    it.id == id
                }
            }


        if (
            handOffId != null
        ) {

            StaffDataStore.lastCollectedPaymentId =
                null
        }


        _uiState.update {

            it.copy(

                payments =
                    latestPayments,

                selectedPayment =

                    handOffPayment
                        ?: latestSelected,

                selectedDeviceCompleted =

                    if (handOffPayment != null) {
                        true
                    } else {
                        deviceCompleted
                    },

                selectedFilter =

                    if (handOffPayment != null) {
                        "Pending"
                    } else {
                        it.selectedFilter
                    },

                collectionMethod =

                    if (handOffPayment != null) {
                        "Bank Transfer"
                    } else {
                        it.collectionMethod
                    }
            )
        }
    }
}


// ============================================================
// PAYMENT SCREEN
// ============================================================

@Composable
fun StaffPaymentsScreen(

    viewModel: StaffPaymentsViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // REFRESH WHEN SCREEN OPENS
    // ========================================================

    LaunchedEffect(Unit) {

        viewModel.onEvent(
            StaffPaymentsEvent
                .RefreshPayments
        )
    }


    val filteredPayments =
        uiState.filteredPayments


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
                "Payments",

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold
        )


        Text(
            text =
                "Payment management",

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


        // ====================================================
        // SUMMARY
        // ====================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            PaymentSummaryCard(

                title =
                    "Pending",

                amount =
                    "RM %.2f".format(
                        uiState.pendingAmount
                    ),

                modifier =
                    Modifier.weight(1f)
            )


            PaymentSummaryCard(

                title =
                    "Paid",

                amount =
                    "RM %.2f".format(
                        uiState.paidAmount
                    ),

                modifier =
                    Modifier.weight(1f)
            )
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
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
                            text = "Search payment",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    BasicTextField(
                        value = uiState.searchText,
                        onValueChange = {
                            viewModel.onEvent(StaffPaymentsEvent.SearchChanged(it))
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

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {

            PaymentFilterButton(

                text =
                    "All",

                selected =
                    uiState.selectedFilter ==
                            "All"
            ) {

                viewModel.onEvent(
                    StaffPaymentsEvent
                        .FilterChanged("All")
                )
            }


            PaymentFilterButton(

                text =
                    "Pending",

                selected =
                    uiState.selectedFilter ==
                            "Pending"
            ) {

                viewModel.onEvent(
                    StaffPaymentsEvent
                        .FilterChanged("Pending")
                )
            }


            PaymentFilterButton(

                text =
                    "Paid",

                selected =
                    uiState.selectedFilter ==
                            "Paid"
            ) {

                viewModel.onEvent(
                    StaffPaymentsEvent
                        .FilterChanged("Paid")
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        Text(

            text =
                "${filteredPayments.size} payment(s)",

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
        // PAYMENT LIST
        // ====================================================

        Column(

            modifier =
                Modifier.fillMaxWidth(),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            filteredPayments
                .forEach { payment ->

                    PaymentCard(

                        payment =
                            payment,

                        onClick = {

                            viewModel.onEvent(
                                StaffPaymentsEvent
                                    .PaymentSelected(
                                        payment
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
    // DETAILS DIALOG
    // ========================================================

    val selectedPayment =
        uiState.selectedPayment


    if (
        selectedPayment != null
    ) {

        PaymentDetailsDialog(

            payment =
                selectedPayment,

            deviceCompleted =
                uiState.selectedDeviceCompleted,

            selectedMethod =
                uiState.collectionMethod,

            onMethodChanged = { method ->

                viewModel.onEvent(
                    StaffPaymentsEvent
                        .CollectionMethodChanged(
                            method
                        )
                )
            },

            onDismiss = {

                viewModel.onEvent(
                    StaffPaymentsEvent
                        .DismissDetails
                )
            },

            onMarkPaid = {

                viewModel.onEvent(
                    StaffPaymentsEvent
                        .MarkPaid
                )
            }
        )
    }
}


// ============================================================
// SUMMARY CARD
// ============================================================

@Composable
fun PaymentSummaryCard(

    title: String,

    amount: String,

    modifier: Modifier =
        Modifier

) {

    Card(

        modifier =
            modifier,

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
                Modifier.padding(16.dp)
        ) {

            Text(

                text =
                    title,

                fontSize =
                    12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            Text(

                text =
                    amount,

                fontSize =
                    19.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// ============================================================
// FILTER BUTTON
// ============================================================

@Composable
fun PaymentFilterButton(

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
                            text == "All" ||
                            text == "Paid"
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
// PAYMENT CARD
// ============================================================

@Composable
fun PaymentCard(

    payment: StaffPayment,

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
                            Icons.Default.CreditCard,

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
                            payment.device,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(

                        text =
                            payment.id,

                        fontSize =
                            12.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                PaymentStatusBadge(
                    status =
                        payment.status
                )
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            PaymentInfoRow(
                label =
                    "Amount",

                value =
                    payment.amount
            )


            PaymentInfoRow(
                label =
                    "Date",

                value =
                    payment.date
            )


            PaymentInfoRow(
                label =
                    "Method",

                value =

                    if (
                        payment.status ==
                        "Pending"
                    ) "N/A"
                    else payment.method
            )


            PaymentInfoRow(
                label =
                    "Appointment",

                value =
                    payment.appointmentId
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            Text(

                text =
                    "Payment ID: ${payment.id}",

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
fun PaymentInfoRow(

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
fun PaymentStatusBadge(

    status: String

) {

    val background =

        if (
            status == "Paid"
        )

            MaterialTheme
                .colorScheme
                .primaryContainer

        else

            MaterialTheme
                .colorScheme
                .secondaryContainer


    val textColor =

        if (
            status == "Paid"
        )

            MaterialTheme
                .colorScheme
                .onPrimaryContainer

        else

            MaterialTheme
                .colorScheme
                .onSecondaryContainer


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
fun PaymentDetailsDialog(

    payment: StaffPayment,

    deviceCompleted: Boolean,

    // Payout method currently selected in the picker.
    selectedMethod: String,

    onMethodChanged: (String) -> Unit,

    onDismiss: () -> Unit,

    onMarkPaid: () -> Unit

) {

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(

                text =
                    "Payment Details",

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

                PaymentDetail(
                    label =
                        "Customer",

                    value =
                        payment.customer
                )


                PaymentDetail(
                    label =
                        "Device",

                    value =
                        payment.device
                )


                PaymentDetail(
                    label =
                        "Appointment",

                    value =
                        payment.appointmentId
                )


                PaymentDetail(
                    label =
                        "Amount",

                    value =
                        payment.amount
                )


                PaymentDetail(
                    label =
                        "Date",

                    value =
                        payment.date
                )


                PaymentDetail(
                    label =
                        "Status",

                    value =
                        payment.status
                )


                PaymentDetail(
                    label =
                        "Method",

                    // Pending payments haven't been
                    // collected yet — show N/A so the
                    // staff clearly sees no method has
                    // been chosen.
                    value =

                        if (
                            payment.status ==
                            "Pending"
                        ) "N/A"
                        else payment.method
                )


                Spacer(
                    modifier =
                        Modifier.height(15.dp)
                )


                if (
                    payment.status ==
                    "Pending"
                ) {

                    if (
                        deviceCompleted
                    ) {

                        // =====================================
                        // PAYMENT METHOD PICKER
                        //
                        // How the company pays the customer
                        // for the recovered parts.
                        // =====================================

                        Text(
                            text =
                                "Payment Method",

                            fontSize =
                                13.sp,

                            fontWeight =
                                FontWeight.Bold
                        )


                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )


                        listOf(
                            "Bank Transfer",
                            "Touch 'n Go",
                            "Cash"
                        ).forEach { method ->

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                RadioButton(
                                    selected =

                                        selectedMethod ==
                                                method,

                                    onClick = {

                                        onMethodChanged(
                                            method
                                        )
                                    }
                                )


                                Text(
                                    text =
                                        method,

                                    fontSize =
                                        14.sp
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )


                        Button(

                            onClick =
                                onMarkPaid,

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.AttachMoney,

                                contentDescription =
                                    null
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )


                            Text(
                                text =
                                    "Done"
                            )
                        }

                    } else {

                        Text(
                            text =
                                "Payment will be available after the device is completed.",

                            fontSize =
                                12.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }


                if (
                    payment.status ==
                    "Paid"
                ) {

                    Text(

                        text =
                            "✓ Payment has been received.",

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
// DETAIL
// ============================================================

@Composable
fun PaymentDetail(

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
// PARSE AMOUNT
// ============================================================

fun parseAmount(
    amount: String
): Double {

    return amount
        .replace(
            "RM",
            ""
        )
        .replace(
            ",",
            ""
        )
        .trim()
        .toDoubleOrNull()
        ?: 0.0
}