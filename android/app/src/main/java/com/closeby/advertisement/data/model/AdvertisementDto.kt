package com.closeby.advertisement.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdvertisementDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("business_name") val businessName: String,
    val title: String,
    val description: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("contact_number") val contactNumber: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("target_radius_meters") val targetRadiusMeters: Int,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    val status: String = "PENDING",
    @SerialName("approved_by") val approvedBy: String? = null,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class AdvertisementInsertDto(
    @SerialName("owner_id") val ownerId: String,
    @SerialName("business_name") val businessName: String,
    val title: String,
    val description: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("contact_number") val contactNumber: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("target_radius_meters") val targetRadiusMeters: Int,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    val status: String = "PENDING"
)

@Serializable
data class AdvertisementAdminUpdateDto(
    val status: String,
    @SerialName("approved_by") val approvedBy: String? = null,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("updated_at") val updatedAt: String
)
