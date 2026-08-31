package com.closeby.app.data.mapper

import com.closeby.app.data.model.ServiceDto
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory

/**
 * Maps Supabase wire models to the servicelisting domain model.
 * Invalid enum values are skipped rather than crashing the app.
 */
object ServiceListingMapper {

    fun toDomainOrNull(dto: ServiceDto): ServiceListing? {
        val category = parseCategory(dto.category) ?: return null
        val subcategory = parseSubcategory(dto.subcategory) ?: return null
        val availability = parseAvailability(dto.availability)
        val priceUnit = parsePriceUnit(dto.priceUnit)
        val provider = dto.providers ?: return null

        return ServiceListing(
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
            price = PriceInfo(
                amount = dto.priceAmount,
                unit = priceUnit,
                isStartingPrice = dto.priceIsStarting
            ),
            contactNumber = provider.phoneNumber,
            providerName = provider.name,
            rating = dto.rating,
            reviewCount = dto.reviewCount,
            isVerifiedProvider = provider.isVerified
        )
    }

    fun toDomainList(dtos: List<ServiceDto>): List<ServiceListing> =
        dtos.mapNotNull(::toDomainOrNull)

    private fun parseCategory(raw: String): ServiceCategory? =
        runCatching { ServiceCategory.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseSubcategory(raw: String): ServiceSubcategory? =
        runCatching { ServiceSubcategory.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseAvailability(raw: String): AvailabilityStatus =
        runCatching { AvailabilityStatus.valueOf(raw.trim().uppercase()) }
            .getOrDefault(AvailabilityStatus.UNAVAILABLE)

    private fun parsePriceUnit(raw: String): PriceUnit =
        runCatching { PriceUnit.valueOf(raw.trim().uppercase()) }
            .getOrDefault(PriceUnit.NONE)
}
