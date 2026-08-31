package com.closeby.availability.data.mapper

import com.closeby.availability.data.model.ProviderAvailabilityDto
import com.closeby.availability.data.model.ProviderAvailabilityUpsertDto
import com.closeby.availability.domain.model.ProviderAvailability
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object AvailabilityMapper {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun toDomain(dto: ProviderAvailabilityDto): ProviderAvailability =
        ProviderAvailability(
            providerId = dto.providerId,
            dayOfWeek = DayOfWeek.of(dto.dayOfWeek),
            isAvailable = dto.isAvailable,
            startTime = dto.startTime?.let { LocalTime.parse(it, timeFormatter) },
            endTime = dto.endTime?.let { LocalTime.parse(it, timeFormatter) }
        )

    fun toUpsertDto(entry: ProviderAvailability): ProviderAvailabilityUpsertDto =
        ProviderAvailabilityUpsertDto(
            providerId = entry.providerId,
            dayOfWeek = entry.dayOfWeek.value,
            isAvailable = entry.isAvailable,
            startTime = entry.startTime?.format(timeFormatter),
            endTime = entry.endTime?.format(timeFormatter)
        )
}
