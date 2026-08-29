package com.example.nekoeco.ui.customer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CreditCard

import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

data class CustomerPaymentsUiState(

    val payments: List<CustomerPayment> =
        emptyList(),

    val selectedFilter: String =
        "All",

    val searchText: String =
        "",

    val selectedPayment: CustomerPayment? =
        null,

    val showDetailsDialog: Boolean =
        false

) {

    val totalPaid: Double
        get() =
            payments
                .filter {
                    it.status == "Paid"
                }
                .sumOf {
                    it.amount
                }


    val pendingAmount: Double
        get() =
            payments
                .filter {
                    it.status == "Pending"
                }
                .sumOf {
                    it.amount
                }


    val totalTransactions: Int
        get() =
            payments.size


    val filteredPayments: List<CustomerPayment>
        get() =

            payments.filter { payment ->

                val matchesFilter =

                    selectedFilter == "All" ||
                            payment.status ==
                            selectedFilter


                val matchesSearch =

                    searchText.isBlank() ||

                            payment.paymentId.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            payment.appointmentId.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            payment.deviceName.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            payment.method.contains(
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

sealed interface CustomerPaymentsEvent {

    data class SearchChanged(
        val text: String
    ) : CustomerPaymentsEvent


    data class FilterChanged(
        val filter: String
    ) : CustomerPaymentsEvent


    data class ViewDetails(
        val payment: CustomerPayment
    ) : CustomerPaymentsEvent


    data object DismissDetails :
        CustomerPaymentsEvent


    data object RefreshPayments :
        CustomerPaymentsEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerPaymentsViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerPaymentsUiState(
                payments =
                    CustomerDataStore
                        .getCurrentCustomerPayments()
            )
        )


    val uiState: StateFlow<CustomerPaymentsUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: CustomerPaymentsEvent
    ) {

        when (event) {

            is CustomerPaymentsEvent.SearchChanged -> {

                _uiState.update {

                    it.copy(
                        searchText =
                            event.text
                    )
                }
            }


            is CustomerPaymentsEvent.FilterChanged -> {

                _uiState.update {

                    it.copy(
                        selectedFilter =
                            event.filter
                    )
                }
            }


            is CustomerPaymentsEvent.ViewDetails -> {

                _uiState.update {

                    it.copy(
                        selectedPayment =
                            event.payment,

                        showDetailsDialog =
                            true
                    )
                }
            }


            CustomerPaymentsEvent.DismissDetails -> {

                _uiState.update {

                    it.copy(
                        selectedPayment =
                            null,

                        showDetailsDialog =
                            false
                    )
                }
            }


            CustomerPaymentsEvent.RefreshPayments -> {

                refreshPayments()
            }
        }
    }


    private fun refreshPayments() {

        _uiState.update {

            it.copy(
                payments =
                    CustomerDataStore
                        .getCurrentCustomerPayments()
            )
        }
    }
}


// ============================================================
// SCREEN
// ============================================================

@Composable
fun CustomerPaymentsScreen(

    viewModel: CustomerPaymentsViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {

        viewModel.onEvent(
            CustomerPaymentsEvent
                .RefreshPayments
        )
    }


    val filteredPayments =
        uiState.filteredPayments


    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
                .padding(
                    horizontal = 20.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }


        item {

            Text(
                text =
                    "Payments",

                fontSize =
                    26.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )


            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )


            Text(
                text =
                    "View your recycling payments",

                fontSize =
                    13.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }


        item {

            PaymentSummarySection(

                totalPaid =
                    uiState.totalPaid,

                pendingAmount =
                    uiState.pendingAmount,

                totalTransactions =
                    uiState.totalTransactions
            )
        }


        item {

            OutlinedTextField(

                value =
                    uiState.searchText,

                onValueChange = {

                    viewModel.onEvent(
                        CustomerPaymentsEvent
                            .SearchChanged(it)
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine =
                    true,

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
                            "Search payments..."
                    )
                },

                shape =
                    RoundedCornerShape(14.dp)
            )
        }


        item {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                PaymentFilterButton(
                    text = "All",
                    selected =
                        uiState.selectedFilter == "All"
                ) {

                    viewModel.onEvent(
                        CustomerPaymentsEvent
                            .FilterChanged("All")
                    )
                }


                PaymentFilterButton(
                    text = "Paid",
                    selected =
                        uiState.selectedFilter == "Paid"
                ) {

                    viewModel.onEvent(
                        CustomerPaymentsEvent
                            .FilterChanged("Paid")
                    )
                }


                PaymentFilterButton(
                    text = "Pending",
                    selected =
                        uiState.selectedFilter == "Pending"
                ) {

                    viewModel.onEvent(
                        CustomerPaymentsEvent
                            .FilterChanged("Pending")
                    )
                }
            }
        }


        item {

            Text(
                text =
                    "${filteredPayments.size} payment(s)",

                fontSize = 13.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }


        item {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Receipt,

                    contentDescription = null,

                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    modifier =
                        Modifier.size(20.dp)
                )


                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )


                Text(
                    text =
                        "Payment History",

                    fontSize = 17.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }


        if (
            filteredPayments.isEmpty()
        ) {

            item {

                EmptyPaymentState()
            }

        } else {

            items(

                items =
                    filteredPayments,

                key = {
                    it.paymentId
                }

            ) { payment ->

                CustomerPaymentCard(

                    payment =
                        payment,

                    onViewDetails = {

                        viewModel.onEvent(
                            CustomerPaymentsEvent
                                .ViewDetails(
                                    payment
                                )
                        )
                    }
                )
            }
        }


        item {

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }


    val selectedPayment =
        uiState.selectedPayment


    if (
        uiState.showDetailsDialog &&
        selectedPayment != null
    ) {

        CustomerPaymentDetailsDialog(

            payment =
                selectedPayment,

            onDismiss = {

                viewModel.onEvent(
                    CustomerPaymentsEvent
                        .DismissDetails
                )
            }
        )
    }
}


// ============================================================
// SUMMARY
// ============================================================

@Composable
private fun PaymentSummarySection(

    totalPaid: Double,

    pendingAmount: Double,

    totalTransactions: Int

) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text =
                "Payment Summary",

            fontSize = 17.sp,

            fontWeight =
                FontWeight.Bold
        )


        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            PaymentSummaryCard(

                value =
                    "RM %.2f".format(
                        totalPaid
                    ),

                label =
                    "Total Paid",

                background =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer,

                valueColor =
                    MaterialTheme
                        .colorScheme
                        .primary,

                modifier =
                    Modifier.weight(1f)
            )


            PaymentSummaryCard(

                value =
                    "RM %.2f".format(
                        pendingAmount
                    ),

                label =
                    "Pending",

                background =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer,

                valueColor =
                    MaterialTheme
                        .colorScheme
                        .secondary,

                modifier =
                    Modifier.weight(1f)
            )
        }


        PaymentSummaryCard(

            value =
                totalTransactions.toString(),

            label =
                "Total Transactions",

            background =
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer,

            valueColor =
                MaterialTheme
                    .colorScheme
                    .tertiary,

            modifier =
                Modifier.fillMaxWidth()
        )
    }
}


// ============================================================
// SUMMARY CARD
// ============================================================

@Composable
private fun PaymentSummaryCard(

    value: String,

    label: String,

    background: Color,

    valueColor: Color,

    modifier: Modifier

) {

    Card(

        modifier =
            modifier.height(88.dp),

        shape =
            RoundedCornerShape(17.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    background
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(14.dp),

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    valueColor,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )


            Text(
                text = label,
                fontSize = 11.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// ============================================================
// FILTER
// ============================================================

@Composable
private fun PaymentFilterButton(

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
// PAYMENT CARD
// ============================================================

@Composable
private fun CustomerPaymentCard(

    payment: CustomerPayment,

    onViewDetails: () -> Unit

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
                defaultElevation =
                    1.dp
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
                            Icons.Default.CreditCard,
                        contentDescription = null,
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        modifier =
                            Modifier.size(23.dp)
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
                            payment.deviceName,
                        fontSize = 15.sp,
                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(
                        text =
                            payment.paymentId,
                        fontSize = 11.sp,
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
                    Modifier.height(13.dp)
            )


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
                        "Payment Amount",
                    fontSize = 12.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Text(
                    text =
                        "RM %.2f".format(
                            payment.amount
                        ),
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            PaymentInfoRow(
                label = "Appointment",
                value =
                    payment.appointmentId
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            PaymentInfoRow(
                label = "Date",
                value =
                    payment.date
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            PaymentInfoRow(
                label = "Method",
                value =
                    payment.method
            )


            Spacer(
                modifier =
                    Modifier.height(13.dp)
            )


            OutlinedButton(

                onClick =
                    onViewDetails,

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Visibility,
                    contentDescription = null,
                    modifier =
                        Modifier.size(17.dp)
                )


                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )


                Text(
                    text =
                        "View Payment Details"
                )
            }
        }
    }
}


// ============================================================
// INFO
// ============================================================

@Composable
private fun PaymentInfoRow(

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
            fontSize = 11.sp,
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
}


// ============================================================
// STATUS
// ============================================================

@Composable
private fun PaymentStatusBadge(

    status: String

) {

    val backgroundColor =

        when (status) {

            "Paid" ->
                MaterialTheme
                    .colorScheme
                    .primaryContainer

            "Pending" ->
                MaterialTheme
                    .colorScheme
                    .secondaryContainer

            "Failed" ->
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

            "Paid" ->
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer

            "Pending" ->
                MaterialTheme
                    .colorScheme
                    .onSecondaryContainer

            "Failed" ->
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
private fun CustomerPaymentDetailsDialog(

    payment: CustomerPayment,

    onDismiss: () -> Unit

) {

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Receipt,
                    contentDescription = null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    modifier =
                        Modifier.size(23.dp)
                )


                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )


                Text(
                    text =
                        "Payment Details",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                PaymentDetailRow(
                    label = "Payment ID",
                    value =
                        payment.paymentId
                )

                PaymentDetailRow(
                    label = "Customer ID",
                    value =
                        payment.customerId
                )

                PaymentDetailRow(
                    label = "Appointment ID",
                    value =
                        payment.appointmentId
                )

                PaymentDetailRow(
                    label = "Device",
                    value =
                        payment.deviceName
                )

                PaymentDetailRow(
                    label = "Amount",
                    value =
                        "RM %.2f".format(
                            payment.amount
                        )
                )

                PaymentDetailRow(
                    label = "Date",
                    value =
                        payment.date
                )

                PaymentDetailRow(
                    label = "Payment Method",
                    value =
                        payment.method
                )

                PaymentDetailRow(
                    label = "Status",
                    value =
                        payment.status
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
        }
    )
}


// ============================================================
// DETAIL ROW
// ============================================================

@Composable
private fun PaymentDetailRow(

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
private fun EmptyPaymentState() {

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(260.dp),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.CreditCard,
                contentDescription = null,
                modifier =
                    Modifier.size(52.dp),
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
                    "No payments found",
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
                    "Your payment history will appear here.",
                fontSize = 12.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}