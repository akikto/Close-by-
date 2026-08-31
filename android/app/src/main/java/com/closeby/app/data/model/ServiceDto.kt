package com.closeby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire model for the `services` table with an embedded `providers` join.
 * Matches [docs/supabase/schema.sql].
 */
@Serializable
data class ServiceDto(
    val id: String,
    @SerialName("provider_id") val providerId: String,
    val category: String,
    val subcategory: String,
    val title: String,
    val description: String = "",
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    val latitude: Double,
    val longitude: Double,
    val availability: String = "AVAILABLE_NOW",
    @SerialName("price_amount") val priceAmount: Double? = null,
    @SerialName("price_unit") val priceUnit: String? = null,
    @SerialName("price_is_starting") val priceIsStarting: Boolean = false,
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("contact_number") val contactNumber: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
    val providers: ProviderEmbedDto? = null
)

/** Provider fields embedded via PostgREST foreign-key join. */
@Serializable
data class ProviderEmbedDto(
    val name: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("is_verified") val isVerified: Boolean = false
)
