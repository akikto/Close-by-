package com.closeby.trust.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportDto(
    val id: String,
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    val reason: String,
    val description: String? = null,
    val status: String = "OPEN",
    @SerialName("resolved_by") val resolvedBy: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("moderation_note") val moderationNote: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ReportInsertDto(
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    val reason: String,
    val description: String? = null
)
