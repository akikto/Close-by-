package com.closeby.trust.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserBlockDto(
    val id: String,
    @SerialName("blocker_id") val blockerId: String,
    @SerialName("blocked_provider_id") val blockedProviderId: String? = null,
    @SerialName("blocked_user_id") val blockedUserId: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class UserBlockInsertDto(
    @SerialName("blocker_id") val blockerId: String,
    @SerialName("blocked_provider_id") val blockedProviderId: String? = null,
    @SerialName("blocked_user_id") val blockedUserId: String? = null
)
