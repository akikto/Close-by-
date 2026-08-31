package com.closeby.feature.servicelisting.domain.usecase

import com.closeby.feature.servicelisting.domain.model.AvailabilityFilter
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.model.ServiceListing

/**
 * Applies a [ServiceFilter] to a list of listings.
 *
 * Radius filtering relies on `distanceInfo.distanceKm`, which must already
 * be populated by the Location module before this use case runs. This use
 * case does not compute distance itself.
 */
class FilterServicesUseCase {

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
}
