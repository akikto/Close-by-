package com.closeby.availability.domain.validation

import com.closeby.availability.domain.model.ProviderAvailability
import java.time.LocalTime

sealed class AvailabilityValidationError(message: String) : Exception(message) {
    data object MissingTimesWhenAvailable :
        AvailabilityValidationError("Start and end time are required when marked available.")
    data object EndBeforeOrEqualStart :
        AvailabilityValidationError("End time must be after start time.")
    data object OverlappingWindows :
        AvailabilityValidationError("Availability windows overlap on the same day.")
    data object DuplicateDay :
        AvailabilityValidationError("Duplicate availability entries for the same day.")
}

/**
 * Stateless validation for [ProviderAvailability] entries. Zero-duration
 * windows (start == end) are not allowed, matching the service request
 * time-range rule.
 */
object AvailabilityValidator {

    fun validate(entry: ProviderAvailability): Result<Unit> {
        if (!entry.isAvailable) return Result.success(Unit)

        val start = entry.startTime
        val end = entry.endTime
        if (start == null || end == null) {
            return Result.failure(AvailabilityValidationError.MissingTimesWhenAvailable)
        }
        return validateTimeRange(start, end)
    }

    fun validateTimeRange(startTime: LocalTime, endTime: LocalTime): Result<Unit> =
        if (!endTime.isAfter(startTime)) {
            Result.failure(AvailabilityValidationError.EndBeforeOrEqualStart)
        } else {
            Result.success(Unit)
        }

    fun validateAll(entries: List<ProviderAvailability>): Result<Unit> {
        val byDay = entries.groupBy { it.dayOfWeek }
        for ((day, dayEntries) in byDay) {
            if (dayEntries.size > 1) {
                return Result.failure(AvailabilityValidationError.DuplicateDay)
            }
            for (entry in dayEntries) {
                validate(entry).onFailure { return Result.failure(it) }
            }
            val available = dayEntries.filter { it.isAvailable }
            if (available.size > 1) {
                return Result.failure(AvailabilityValidationError.OverlappingWindows)
            }
        }
        return Result.success(Unit)
    }
}
