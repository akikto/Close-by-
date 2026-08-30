package com.closeby.availability.domain.validation

import com.closeby.availability.domain.model.ProviderAvailability
import java.time.LocalTime

sealed class AvailabilityValidationError(message: String) : Exception(message) {
    data object MissingTimesWhenAvailable :
        AvailabilityValidationError("Start and end time are required when marked available.")
    data object EndBeforeOrEqualStart :
        AvailabilityValidationError("End time must be after start time.")
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
        for (entry in entries) {
            validate(entry).onFailure { return Result.failure(it) }
        }
        return Result.success(Unit)
    }
}
