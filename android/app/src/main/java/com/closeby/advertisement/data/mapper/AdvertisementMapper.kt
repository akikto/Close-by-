package com.closeby.advertisement.data.mapper

import com.closeby.advertisement.data.model.AdvertisementDto
import com.closeby.advertisement.data.model.AdvertisementInsertDto
import com.closeby.advertisement.domain.model.AdStatus
import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.advertisement.domain.model.AdvertisementInput
import java.time.Instant

object AdvertisementMapper {

    fun toDomain(dto: AdvertisementDto): Advertisement? {
        val status = parseStatus(dto.status) ?: return null
        return Advertisement(
            id = dto.id,
            ownerId = dto.ownerId,
            businessName = dto.businessName,
            title = dto.title,
            description = dto.description,
            imageUrl = dto.imageUrl,
            contactNumber = dto.contactNumber,
            latitude = dto.latitude,
            longitude = dto.longitude,
            targetRadiusMeters = dto.targetRadiusMeters,
            startAt = parseInstant(dto.startAt),
            endAt = parseInstant(dto.endAt),
            status = status,
            approvedBy = dto.approvedBy,
            approvedAt = dto.approvedAt?.let(::parseInstant),
            rejectionReason = dto.rejectionReason,
            createdAt = parseInstant(dto.createdAt),
            updatedAt = parseInstant(dto.updatedAt)
        )
    }

    fun toInsertDto(ownerId: String, input: AdvertisementInput): AdvertisementInsertDto =
        AdvertisementInsertDto(
            ownerId = ownerId,
            businessName = input.businessName.trim(),
            title = input.title.trim(),
            description = input.description.trim(),
            imageUrl = input.imageUrl,
            contactNumber = input.contactNumber.trim(),
            latitude = input.latitude,
            longitude = input.longitude,
            targetRadiusMeters = input.targetRadiusMeters,
            startAt = Instant.ofEpochMilli(input.startAt).toString(),
            endAt = Instant.ofEpochMilli(input.endAt).toString(),
            status = AdStatus.PENDING.name
        )

    private fun parseStatus(raw: String): AdStatus? =
        runCatching { AdStatus.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseInstant(raw: String): Long =
        Instant.parse(raw).toEpochMilli()
}
