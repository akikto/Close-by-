package com.closeby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderDto(
    val id: String,
    val name: String,
    val category: String,
    @SerialName("phone_number") val phoneNumber: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0
)

@Serializable
data class ProviderInsertDto(
    val name: String,
    val category: String,
    @SerialName("phone_number") val phoneNumber: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("user_id") val userId: String
)

@Serializable
data class ProviderUpdateDto(
    val name: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
