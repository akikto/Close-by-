package com.closeby.app.data.model

import kotlinx.serialization.Serializable

/**
 * Wire model matching the `providers` table shape in Supabase/PostgreSQL.
 * Kept minimal for the base project; expand alongside the real schema.
 */
@Serializable
data class ProviderDto(
    val id: String,
    val name: String,
    val category: String,
    val phone_number: String,
    val latitude: Double,
    val longitude: Double
)
