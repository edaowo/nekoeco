package com.example.nekoeco.data.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PaymentRow(
    val id: String,
    @SerialName("payment_id")
    val paymentId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("appointment_id")
    val appointmentId: String? = null,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_name")
    val deviceName: String,
    val amount: Double,
    val date: String,
    val method: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)


@Serializable
data class NewPaymentRow(
    @SerialName("payment_id")
    val paymentId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("appointment_id")
    val appointmentId: String? = null,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_name")
    val deviceName: String,
    val amount: Double,
    val date: String,
    val method: String,
    val status: String
)