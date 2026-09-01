package com.example.nekoeco.data.db

import android.util.Log
import com.example.nekoeco.data.auth.IdOnlyRow
import com.example.nekoeco.data.auth.SupabaseClientProvider
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns

class AppointmentRepository(
    private val postgrest: Postgrest =
        SupabaseClientProvider.client.pluginManager.getPlugin(Postgrest)
) {


    suspend fun insertAppointment(
        appt: NewAppointmentRow
    ): AppointmentRow {

        postgrest
            .from("appointments")
            .insert(appt)


        return postgrest
            .from("appointments")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("appointment_id", appt.appointmentId)
                }
                limit(1)
            }
            .decodeList<AppointmentRow>()
            .firstOrNull()
            ?: throw IllegalStateException(
                "Appointment insert reported success but row not found: ${appt.appointmentId}"
            )
    }


    suspend fun updateStatus(
        appointmentId: String,
        newStatus: String
    ): Boolean {

        runCatching {

            postgrest
                .from("appointments")
                .update(
                    {
                        set("status", newStatus)
                    }
                ) {
                    filter {
                        eq("appointment_id", appointmentId)
                    }
                }

        }.onFailure {

            return false
        }


        val matched = postgrest
            .from("appointments")
            .select(
                columns = Columns.list("appointment_id")
            ) {
                filter {
                    eq("appointment_id", appointmentId)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()


        return matched.isNotEmpty()
    }


    suspend fun updateDateTime(
        appointmentId: String,
        newDate: String,
        newTime: String
    ): Boolean {

        return try {

            postgrest
                .from("appointments")
                .update(
                    {
                        set("date", newDate)
                        set("time", newTime)
                    }
                ) {
                    filter {
                        eq("appointment_id", appointmentId)
                    }
                }

            true

        } catch (t: Throwable) {

            false
        }
    }


    // ========================================================
    // UPDATE DEVICE NAME — appointments carry a copied
    // device_name; when the customer renames the device,
    // every row for that device_id must follow or a
    // reload pulls the stale name back into Schedule.
    // ========================================================

    suspend fun updateDeviceName(
        deviceId: String,
        newName: String
    ): Boolean {

        return try {

            postgrest
                .from("appointments")
                .update(
                    {
                        set("device_name", newName)
                    }
                ) {
                    filter {
                        eq("device_id", deviceId)
                    }
                }

            true

        } catch (t: Throwable) {

            false
        }
    }


    suspend fun listByCustomer(
        customerId: String
    ): List<AppointmentRow> {

        return postgrest
            .from("appointments")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("customer_id", customerId)
                }
            }
            .decodeList<AppointmentRow>()
    }


    suspend fun listAll(): List<AppointmentRow> {

        return postgrest
            .from("appointments")
            .select(
                columns = Columns.ALL
            )
            .decodeList<AppointmentRow>()
    }


    // ========================================================
    // DELETE BY DEVICE — remove every appointment row
    // referencing a given device_id. Returns the number of
    // rows actually deleted.
    // ========================================================

    suspend fun deleteByDevice(
        deviceId: String
    ): Int {

        return try {

            postgrest
                .from("appointments")
                .delete(
                    {
                        filter {
                            eq("device_id", deviceId)
                        }
                        select()
                    }
                )
                .decodeList<AppointmentRow>()
                .size

        } catch (t: Throwable) {

            Log.e(
                "NekoEcoDB",
                "deleteByDevice($deviceId) failed",
                t
            )
            0
        }
    }
}


object AppointmentRepo {

    val instance: AppointmentRepository =
        AppointmentRepository()
}