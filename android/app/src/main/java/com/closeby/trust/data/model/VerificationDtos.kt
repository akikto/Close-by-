package com.closeby.trust.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificationSubmissionDto(
    val id: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("submitted_by") val submittedBy: String? = null,
    @SerialName("business_name") val businessName: String,
    @SerialName("contact_phone") val contactPhone: String,
    val description: String? = null,
    @SerialName("document_url") val documentUrl: String? = null,
    val status: String = "PENDING",
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("reviewed_by") val reviewedBy: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class VerificationInsertDto(
    @SerialName("provider_id") val providerId: String,
    @SerialName("submitted_by") val submittedBy: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("contact_phone") val contactPhone: String,
    val description: String? = null,
    @SerialName("document_url") val documentUrl: String? = null,
    val status: String = "PENDING"
)
