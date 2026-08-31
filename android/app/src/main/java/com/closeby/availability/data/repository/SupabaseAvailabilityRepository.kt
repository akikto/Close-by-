package com.closeby.availability.data.repository

import com.closeby.availability.data.mapper.AvailabilityMapper
import com.closeby.availability.data.remote.AvailabilityRemoteDataSource
import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.availability.domain.repository.AvailabilityRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class SupabaseAvailabilityRepository(
    private val remote: AvailabilityRemoteDataSource = AvailabilityRemoteDataSource()
) : AvailabilityRepository {

    override suspend fun getProviderAvailability(providerId: String): Result<List<ProviderAvailability>> =
        runCatching {
            val rows = remote.getByProvider(providerId)
            if (rows.isEmpty()) {
                return@runCatching defaultWeek(providerId)
            }
            rows.map(AvailabilityMapper::toDomain)
        }

    override suspend fun saveAvailability(availability: List<ProviderAvailability>): Result<Unit> =
        runCatching {
            val providerId = availability.firstOrNull()?.providerId
                ?: throw IllegalArgumentException("No availability entries.")
            val dtos = availability.map(AvailabilityMapper::toUpsertDto)
            remote.replaceAll(providerId, dtos)
        }

    override suspend fun isAvailable(
        providerId: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Boolean> = runCatching {
        val entries = getProviderAvailability(providerId).getOrThrow()
        val dayEntry = entries.firstOrNull { it.dayOfWeek == date.dayOfWeek }
            ?: return@runCatching false
        if (!dayEntry.isAvailable) return@runCatching false
        val start = dayEntry.startTime ?: return@runCatching false
        val end = dayEntry.endTime ?: return@runCatching false
        !startTime.isBefore(start) && !endTime.isAfter(end)
    }

    private fun defaultWeek(providerId: String): List<ProviderAvailability> =
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
