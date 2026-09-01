package com.example.nekoeco.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// ============================================================
// Row shape used for SELECT results.
//
// MUST include every column that the table returns, otherwise
// kotlinx-serialization throws on the unknown key and the
// whole query appears to "fail" to the caller. PostgREST
// returns all SELECTed columns including created_at and
// updated_at, so those are declared here too.
// ============================================================

@Serializable
data class UserRow(
    val id: String,
    val role: String,
    val email: String? = null,
    @SerialName("staff_id")
    val staffId: String? = null,
    val name: String,
    @SerialName("password_hash")
    val passwordHash: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)


// ============================================================
// Row shape used for INSERT — omits `id` so the Postgres
// gen_random_uuid() default fires. Sending id="" was being
// rejected as an invalid UUID and (because of the catch-all
// in CustomerDataStore) reported to the user as "email
// already registered".
// ============================================================

@Serializable
data class NewUserRow(
    val role: String,
    val email: String? = null,
    @SerialName("staff_id")
    val staffId: String? = null,
    val name: String,
    @SerialName("password_hash")
    val passwordHash: String
)