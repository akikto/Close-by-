package com.closeby.availability.domain.repository

import com.closeby.availability.domain.model.ProviderAvailability
import java.time.LocalDate
import java.time.LocalTime

/**
 * Backend-agnostic repository contract for provider availability.
 *
 * Implementations must verify at the backend/repository level that a
 * provider can only save their own availability (see SECURITY in the
 * contract) — this interface does not assume any particular backend.
 */
interface AvailabilityRepository {

    suspend fun getProviderAvailability(providerId: String): Result<List<ProviderAvailability>>

    suspend fun saveAvailability(availability: List<ProviderAvailability>): Result<Unit>

    suspend fun isAvailable(
        providerId: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Boolean>
}
