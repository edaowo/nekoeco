package com.example.nekoeco.data.db

import android.util.Log
import com.example.nekoeco.data.auth.IdOnlyRow
import com.example.nekoeco.data.auth.SupabaseClientProvider
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns

class PaymentRepository(
    private val postgrest: Postgrest =
        SupabaseClientProvider.client.pluginManager.getPlugin(Postgrest)
) {


    suspend fun insertPayment(
        payment: NewPaymentRow
    ): PaymentRow {

        postgrest
            .from("payments")
            .insert(payment)


        return postgrest
            .from("payments")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("payment_id", payment.paymentId)
                }
                limit(1)
            }
            .decodeList<PaymentRow>()
            .firstOrNull()
            ?: throw IllegalStateException(
                "Payment insert reported success but row not found: ${payment.paymentId}"
            )
    }


    suspend fun updateStatus(
        paymentId: String,
        newStatus: String,
        // Optional — when provided, the payment method
        // (Bank Transfer / Touch 'n Go / Cash) is written
        // in the same update. Used by the staff payment
        // collection flow.
        newMethod: String? = null,
        // Optional — when provided, the payout amount is
        // rewritten too (staff inspection can change the
        // approved total after the payment row exists).
        newAmount: Double? = null
    ): Boolean {

        runCatching {

            postgrest
                .from("payments")
            .update(
                {
                    set("status", newStatus)

                    if (newMethod != null) {
                        set("method", newMethod)
                    }

                    if (newAmount != null) {
                        set("amount", newAmount)
                    }
                }
            ) {
                filter {
                    eq("payment_id", paymentId)
                }
            }

        }.onFailure {

            return false
        }


        val matched = postgrest
            .from("payments")
            .select(
                columns = Columns.list("payment_id")
            ) {
                filter {
                    eq("payment_id", paymentId)
                }
                limit(1)
            }
            .decodeList<IdOnlyRow>()


        return matched.isNotEmpty()
    }


    // ========================================================
    // UPDATE DEVICE NAME — payments carry a copied
    // device_name; when the customer renames the device,
    // every row for that device_id must follow or a
    // reload pulls the stale name back.
    // ========================================================

    suspend fun updateDeviceName(
        deviceId: String,
        newName: String
    ): Boolean {

        return try {

            postgrest
                .from("payments")
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
    ): List<PaymentRow> {

        return postgrest
            .from("payments")
            .select(
                columns = Columns.ALL
            ) {
                filter {
                    eq("customer_id", customerId)
                }
            }
            .decodeList<PaymentRow>()
    }


    suspend fun listAll(): List<PaymentRow> {

        return postgrest
            .from("payments")
            .select(
                columns = Columns.ALL
            )
            .decodeList<PaymentRow>()
    }


    // ========================================================
    // DELETE BY DEVICE — remove every payment row
    // referencing a given device_id.
    // ========================================================

    suspend fun deleteByDevice(
        deviceId: String
    ): Int {

        return try {

            postgrest
                .from("payments")
                .delete(
                    {
                        filter {
                            eq("device_id", deviceId)
                        }
                        select()
                    }
                )
                .decodeList<PaymentRow>()
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


object PaymentRepo {

    val instance: PaymentRepository =
        PaymentRepository()
}