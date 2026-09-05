package com.closeby.feature.servicelisting.domain.usecase

import com.closeby.availability.domain.repository.AvailabilityRepository
import com.closeby.feature.servicelisting.domain.model.AvailabilityFilter
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import java.time.LocalDate
import java.time.LocalTime

/**
 * Applies synchronous filters, then optionally checks provider calendar availability
 * for [AvailabilityFilter.AVAILABLE_ON_DATE].
 */
class FilterServicesUseCase(
    private val availabilityRepository: AvailabilityRepository? = null
) {

    operator fun invoke(listings: List<ServiceListing>, filter: ServiceFilter): List<ServiceListing> {
        var result = listings

        filter.category?.let { category ->
            result = result.filter { it.category == category }
        }

        filter.subcategory?.let { subcategory ->
            result = result.filter { it.subcategory == subcategory }
        }

        filter.radiusKm?.let { radius ->
            result = result.filter { listing ->
                val distance = listing.distanceInfo?.distanceKm
                distance != null && distance <= radius.km
            }
        }

        filter.customRadiusKm?.let { customKm ->
            result = result.filter { listing ->
                val distance = listing.distanceInfo?.distanceKm
                distance != null && distance <= customKm
            }
        }

        when (filter.availability) {
            AvailabilityFilter.AVAILABLE_NOW ->
                result = result.filter { it.availability == AvailabilityStatus.AVAILABLE_NOW }
            AvailabilityFilter.AVAILABLE_ON_DATE ->
                result = result.filter { it.availability != AvailabilityStatus.UNAVAILABLE }
            AvailabilityFilter.ANY -> Unit
        }

        filter.minRating?.let { minRating ->
            result = result.filter { it.rating >= minRating }
        }

        filter.maxPrice?.let { maxPrice ->
            result = result.filter { it.price.amount <= maxPrice }
        }

        return result
    }

    suspend fun applyDateAvailability(
        listings: List<ServiceListing>,
        date: LocalDate
    ): List<ServiceListing> {
        val repository = availabilityRepository ?: return listings
        return listings.filter { listing ->
            repository.isAvailable(
                providerId = listing.providerId,
                date = date,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0)
            ).getOrDefault(false)
        }
    }
}
