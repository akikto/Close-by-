package com.closeby.feature.servicelisting.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedServiceDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SavedServiceInsertDto(
    @SerialName("user_id") val userId: String,
    @SerialName("service_id") val serviceId: String
)
