package com.closeby.availability.data.repository

import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.availability.domain.repository.AvailabilityRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap

class MockAvailabilityRepository : AvailabilityRepository {

    private val store = ConcurrentHashMap<String, List<ProviderAvailability>>()

    override suspend fun getProviderAvailability(providerId: String): Result<List<ProviderAvailability>> =
        Result.success(
            store.getOrPut(providerId) {
                DayOfWeek.entries.map { day ->
                    ProviderAvailability(
                        providerId = providerId,
                        dayOfWeek = day,
                        isAvailable = day != DayOfWeek.SUNDAY,
                        startTime = if (day == DayOfWeek.SUNDAY) null else LocalTime.of(8, 0),
                        endTime = if (day == DayOfWeek.SUNDAY) null else LocalTime.of(18, 0)
                    )
                }
            }
        )

    override suspend fun saveAvailability(availability: List<ProviderAvailability>): Result<Unit> {
        val providerId = availability.firstOrNull()?.providerId ?: return Result.failure(
            IllegalArgumentException("No availability entries.")
        )
        store[providerId] = availability
        return Result.success(Unit)
    }

    override suspend fun isAvailable(
        providerId: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Boolean> = runCatching {
        val entries = getProviderAvailability(providerId).getOrThrow()
        val dayEntry = entries.first { it.dayOfWeek == date.dayOfWeek }
        if (!dayEntry.isAvailable) return@runCatching false
        val start = dayEntry.startTime ?: return@runCatching false
        val end = dayEntry.endTime ?: return@runCatching false
        !startTime.isBefore(start) && !endTime.isAfter(end)
    }
}
