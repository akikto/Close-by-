package com.closeby.feature.provider.data.mapper

import com.closeby.app.data.model.ProviderDto
import com.closeby.app.data.model.ServiceDto
import com.closeby.feature.provider.domain.model.ManagedService
import com.closeby.feature.provider.domain.model.ManagedServiceSummary
import com.closeby.feature.provider.domain.model.ProviderProfile
import com.closeby.feature.provider.domain.model.ServiceFormInput
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.trust.domain.model.VerificationStatus

object ProviderProfileMapper {

    fun toProfile(
        provider: ProviderDto,
        services: List<ManagedServiceSummary>,
        availability: List<ProviderAvailability>,
        distanceLabel: String?,
        isOwnProfile: Boolean
    ): ProviderProfile? {
        val category = ManagedServiceMapper.parseCategory(provider.category) ?: return null
        return ProviderProfile(
            id = provider.id,
            name = provider.name,
            profileImageUrl = provider.profileImageUrl,
            category = category,
            isVerified = provider.verificationStatus
                ?.let { VerificationStatus.fromRaw(it).isVerified }
                ?: provider.isVerified,
            rating = provider.rating,
            reviewCount = provider.reviewCount,
            phoneNumber = if (isOwnProfile) provider.phoneNumber else null,
            distanceLabel = distanceLabel,
            services = services,
            availability = availability,
            isOwnProfile = isOwnProfile
        )
    }
}

object ManagedServiceMapper {

    fun parseCategory(raw: String): ServiceCategory? =
        runCatching { ServiceCategory.valueOf(raw.trim().uppercase()) }.getOrNull()

    fun toDomain(dto: ServiceDto): ManagedService? {
        val category = parseCategory(dto.category) ?: return null
        val subcategory = parseSubcategory(dto.subcategory) ?: return null
        val availability = parseAvailability(dto.availability)
        val provider = dto.providers
        val contact = dto.contactNumber ?: provider?.phoneNumber ?: return null
        val price = dto.priceAmount?.let { amount ->
            PriceInfo(
                amount = amount,
                unit = parsePriceUnit(dto.priceUnit),
                isStartingPrice = dto.priceIsStarting
            )
        }

        return ManagedService(
            id = dto.id,
            providerId = dto.providerId,
            category = category,
            subcategory = subcategory,
            title = dto.title,
            description = dto.description,
            imageUrls = dto.imageUrls,
            latitude = dto.latitude,
            longitude = dto.longitude,
            availability = availability,
            price = price,
            contactNumber = contact,
            isActive = dto.isActive,
            isDeleted = dto.deletedAt != null,
            rating = dto.rating,
            reviewCount = dto.reviewCount
        )
    }

    fun toSummary(dto: ServiceDto): ManagedServiceSummary? {
        val category = parseCategory(dto.category) ?: return null
        val subcategory = parseSubcategory(dto.subcategory) ?: return null
        val price = dto.priceAmount?.let { amount ->
            PriceInfo(
                amount = amount,
                unit = parsePriceUnit(dto.priceUnit),
                isStartingPrice = dto.priceIsStarting
            )
        }
        return ManagedServiceSummary(
            id = dto.id,
            title = dto.title,
            category = category,
            subcategory = subcategory,
            price = price,
            availability = parseAvailability(dto.availability),
            isActive = dto.isActive
        )
    }

    fun toInsertDto(providerId: String, input: ServiceFormInput) =
        com.closeby.app.data.model.ServiceInsertDto(
            providerId = providerId,
            category = input.category.name,
            subcategory = input.subcategory.name,
            title = input.title.trim(),
            description = input.description.trim(),
            imageUrls = input.imageUrls,
            latitude = input.latitude,
            longitude = input.longitude,
            availability = input.availability.name,
            priceAmount = input.priceAmount,
            priceUnit = input.priceUnit?.name,
            priceIsStarting = input.priceIsStarting,
            contactNumber = input.contactNumber.trim()
        )

    fun toUpdateDto(input: ServiceFormInput) =
        com.closeby.app.data.model.ServiceUpdateDto(
            category = input.category.name,
            subcategory = input.subcategory.name,
            title = input.title.trim(),
            description = input.description.trim(),
            imageUrls = input.imageUrls,
            latitude = input.latitude,
            longitude = input.longitude,
            availability = input.availability.name,
            priceAmount = input.priceAmount,
            priceUnit = input.priceUnit?.name,
            priceIsStarting = input.priceIsStarting,
            contactNumber = input.contactNumber.trim()
        )

    private fun parseSubcategory(raw: String): ServiceSubcategory? =
        runCatching { ServiceSubcategory.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseAvailability(raw: String): AvailabilityStatus =
        runCatching { AvailabilityStatus.valueOf(raw.trim().uppercase()) }
            .getOrDefault(AvailabilityStatus.UNAVAILABLE)

    private fun parsePriceUnit(raw: String?): PriceUnit =
        raw?.let {
            runCatching { PriceUnit.valueOf(it.trim().uppercase()) }.getOrNull()
        } ?: PriceUnit.NONE
}
