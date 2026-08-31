package com.closeby.request.domain.validation

import com.closeby.availability.domain.repository.AvailabilityRepository
import java.time.LocalDate
import java.time.LocalTime

object RequestAvailabilityChecker {

    suspend fun validateProviderAvailable(
        repository: AvailabilityRepository,
        providerId: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Unit> {
        return repository.isAvailable(providerId, date, startTime, endTime)
            .fold(
                onSuccess = { available ->
                    if (available) Result.success(Unit)
                    else Result.failure(
                        ServiceRequestValidationError.ProviderUnavailable
                    )
                },
                onFailure = { Result.failure(it) }
            )
    }
}
