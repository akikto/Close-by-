package com.closeby.trust.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("reviewer_id") val reviewerId: String,
    @SerialName("reviewee_id") val revieweeId: String,
    @SerialName("reviewer_role") val reviewerRole: String,
    @SerialName("overall_rating") val overallRating: Int,
    @SerialName("service_quality") val serviceQuality: Int? = null,
    val behaviour: Int? = null,
    val reliability: Int? = null,
    val professionalism: Int? = null,
    val comment: String? = null,
    @SerialName("moderation_status") val moderationStatus: String = "VISIBLE",
    @SerialName("is_visible") val isVisible: Boolean = true,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ReviewInsertDto(
    @SerialName("request_id") val requestId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("reviewer_id") val reviewerId: String,
    @SerialName("reviewee_id") val revieweeId: String,
    @SerialName("reviewer_role") val reviewerRole: String,
    @SerialName("overall_rating") val overallRating: Int,
    @SerialName("service_quality") val serviceQuality: Int? = null,
    val behaviour: Int? = null,
    val reliability: Int? = null,
    val professionalism: Int? = null,
    val comment: String? = null
)
