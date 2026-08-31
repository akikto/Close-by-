package com.closeby.availability.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderAvailabilityDto(
    val id: String? = null,
    @SerialName("provider_id") val providerId: String,
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("is_available") val isAvailable: Boolean,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null
)

@Serializable
data class ProviderAvailabilityUpsertDto(
    @SerialName("provider_id") val providerId: String,
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("is_available") val isAvailable: Boolean,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null
)
