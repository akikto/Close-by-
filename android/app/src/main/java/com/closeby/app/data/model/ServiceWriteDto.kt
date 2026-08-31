package com.closeby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceInsertDto(
    @SerialName("provider_id") val providerId: String,
    val category: String,
    val subcategory: String,
    val title: String,
    val description: String,
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    val latitude: Double,
    val longitude: Double,
    val availability: String,
    @SerialName("price_amount") val priceAmount: Double? = null,
    @SerialName("price_unit") val priceUnit: String? = null,
    @SerialName("price_is_starting") val priceIsStarting: Boolean = false,
    @SerialName("contact_number") val contactNumber: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class ServiceUpdateDto(
    val category: String,
    val subcategory: String,
    val title: String,
    val description: String,
    @SerialName("image_urls") val imageUrls: List<String>,
    val latitude: Double,
    val longitude: Double,
    val availability: String,
    @SerialName("price_amount") val priceAmount: Double? = null,
    @SerialName("price_unit") val priceUnit: String? = null,
    @SerialName("price_is_starting") val priceIsStarting: Boolean = false,
    @SerialName("contact_number") val contactNumber: String,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ServiceActiveUpdateDto(
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ServiceDeleteDto(
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String
)
