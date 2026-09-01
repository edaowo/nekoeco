package com.example.nekoeco.data.db

import android.util.Log
import com.example.nekoeco.data.auth.IdOnlyRow
import com.example.nekoeco.data.auth.SupabaseClientProvider
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonArray

class DeviceRepository(
    private val postgrest: Postgrest =
        SupabaseClientProvider.client.pluginManager.getPlugin(Postgrest)
) {


    // ========================================================
    // INSERT (two-step: fire insert, then fetch by device_id
    // to get the full row back — mirrors the auth-layer pattern
    // because PostgREST insert returns no body without the
    // Prefer: return=representation header which the SDK
    // doesn't set reliably).
    // ========================================================

    suspend fun insertDevice(
        device: NewDeviceRow
    ): DeviceRow {

        postgrest
            .from("devices")
            .insert(device)


        return postgrest
            .from("devices")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("device_id", device.deviceId)
                }
                limit(1)
            }
            .decodeList<DeviceRow>()
            .firstOrNull()
            ?: throw IllegalStateException(
                "Device insert reported success but row not found: ${device.deviceId}"
            )
    }


    // ========================================================
    // UPDATE STATUS — set status column by device_id.
    // Returns true if a row actually matched.
    // ========================================================

    suspend fun updateStatus(
        deviceId: String,
        newStatus: String
    ): Boolean {

        runCatching {

            postgrest
                .from("devices")
                .update(
                    {
                        set("status", newStatus)
                    }
                ) {
                    filter {
                        eq("device_id", deviceId)
                    }
                }

        }.onFailure {

            return false
        }


        val matched = postgrest
            .from("devices")
            .select(
                columns = Columns.list("device_id")
            ) {
                filter {
                    eq("device_id", deviceId)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()


        return matched.isNotEmpty()
    }


    // ========================================================
    // UPDATE INSPECTION — persist the staff-approved parts
    // (accessories JSON, each entry carrying an "approved"
    // flag) and the recomputed recovery value by device_id.
    // Returns true if a row actually matched.
    // ========================================================

    suspend fun updateInspection(
        deviceId: String,
        accessories: JsonArray,
        recoveryValue: Double
    ): Boolean {

        runCatching {

            postgrest
                .from("devices")
                .update(
                    {
                        set("accessories", accessories)
                        set("recovery_value", recoveryValue)
                    }
                ) {
                    filter {
                        eq("device_id", deviceId)
                    }
                }

        }.onFailure {

            return false
        }


        val matched = postgrest
            .from("devices")
            .select(
                columns = Columns.list("device_id")
            ) {
                filter {
                    eq("device_id", deviceId)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()


        return matched.isNotEmpty()
    }


    // ========================================================
    // UPDATE NAME — set device_name column by device_id.
    // Returns true if a row actually matched.
    // ========================================================

    suspend fun updateName(
        deviceId: String,
        newName: String
    ): Boolean {

        runCatching {

            postgrest
                .from("devices")
                .update(
                    {
                        set("device_name", newName)
                    }
                ) {
                    filter {
                        eq("device_id", deviceId)
                    }
                }

        }.onFailure {

            return false
        }


        val matched = postgrest
            .from("devices")
            .select(
                columns = Columns.list("device_id")
            ) {
                filter {
                    eq("device_id", deviceId)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()


        return matched.isNotEmpty()
    }


    // ========================================================
    // DELETE — remove device by device_id.
    //
    // We must call `select()` inside the delete block so
    // PostgREST returns the deleted rows in the response body
    // (return=representation). Without it PostgREST replies
    // with `return=minimal` (204 No Content) and the SDK
    // can't tell us whether anything was actually deleted.
    //
    // Returns true if the server reported at least one row
    // deleted. False means: nothing matched, or the call
    // threw — caller should treat that as a real failure.
    // ========================================================

    suspend fun deleteDevice(
        deviceId: String
    ): Boolean {

        return try {

            val deleted =
                postgrest
                    .from("devices")
                    .delete(
                        {
                            filter {
                                eq("device_id", deviceId)
                            }
                            select()
                        }
                    )
                    .decodeList<DeviceRow>()

            deleted.isNotEmpty()

        } catch (t: Throwable) {

            Log.e(
                "NekoEcoDB",
                "deleteDevice($deviceId) failed",
                t
            )
            false
        }
    }


    // ========================================================
    // LIST — for one customer (used by CustomerDataStore
    // loader) or for all customers (used by StaffDataStore).
    // ========================================================

    suspend fun listByCustomer(
        customerId: String
    ): List<DeviceRow> {

        return postgrest
            .from("devices")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("customer_id", customerId)
                }
            }
            .decodeList<DeviceRow>()
    }


    suspend fun listAll(): List<DeviceRow> {

        return postgrest
            .from("devices")
            .select(
                columns = Columns.ALL
            )
            .decodeList<DeviceRow>()
    }
}


object DeviceRepo {

    val instance: DeviceRepository =
        DeviceRepository()
}