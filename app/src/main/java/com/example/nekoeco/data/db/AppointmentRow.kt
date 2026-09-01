package com.example.nekoeco.data.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AppointmentRow(
    val id: String,
    @SerialName("appointment_id")
    val appointmentId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_name")
    val deviceName: String,
    val date: String,
    val time: String,
    val company: String,
    val location: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)


@Serializable
data class NewAppointmentRow(
    @SerialName("appointment_id")
    val appointmentId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_name")
    val deviceName: String,
    val date: String,
    val time: String,
    val company: String,
    val location: String,
    val status: String
)