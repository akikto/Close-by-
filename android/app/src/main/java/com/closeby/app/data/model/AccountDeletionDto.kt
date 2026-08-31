package com.closeby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDeletionRequestDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val status: String,
    @SerialName("requested_at") val requestedAt: String
)

@Serializable
data class AccountDeletionInsertDto(
    @SerialName("user_id") val userId: String,
    val reason: String? = null
)
