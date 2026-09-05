package com.closeby.admin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDeletionRequestDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val reason: String? = null,
    val status: String = "PENDING",
    @SerialName("requested_at") val requestedAt: String,
    @SerialName("processed_at") val processedAt: String? = null
)

@Serializable
data class AccountDeletionStatusUpdateDto(
    val status: String,
    @SerialName("processed_at") val processedAt: String
)
