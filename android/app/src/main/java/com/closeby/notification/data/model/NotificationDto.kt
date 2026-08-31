package com.closeby.notification.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("reference_type") val referenceType: String? = null,
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class NotificationInsertDto(
    @SerialName("user_id") val userId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("reference_type") val referenceType: String? = null,
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false
)

@Serializable
data class NotificationReadUpdateDto(
    @SerialName("is_read") val isRead: Boolean = true
)
