package com.closeby.availability.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * A provider's declared availability for one day of the week.
 *
 * When [isAvailable] is false, [startTime]/[endTime] are ignored/should be null.
 */
data class ProviderAvailability(
    val providerId: String,
    val dayOfWeek: DayOfWeek,
    val isAvailable: Boolean,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null
)
