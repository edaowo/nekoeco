package com.example.nekoeco.data.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


// ============================================================
// Row shape returned by SELECT on public.devices. Includes
// every column so the in-memory list (which has the same
// fields) can be rebuilt from this.
// ============================================================

@Serializable
data class DeviceRow(
    val id: String,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("customer_name")
    val customerName: String,
    @SerialName("device_name")
    val deviceName: String,
    val category: String,
    val date: String,
    val status: String,
    val accessories: JsonElement,
    @SerialName("recovery_value")
    val recoveryValue: Double,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)


// ============================================================
// Row shape used for INSERT. Omits `id` (DB default) and the
// timestamps (DB defaults) so we don't send empty strings
// for uuid / timestamptz columns.
// ============================================================

@Serializable
data class NewDeviceRow(
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("customer_name")
    val customerName: String,
    @SerialName("device_name")
    val deviceName: String,
    val category: String,
    val date: String,
    val status: String,
    val accessories: JsonElement,
    @SerialName("recovery_value")
    val recoveryValue: Double
)