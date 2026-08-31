package com.closeby.feature.servicelisting.domain.usecase

import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.SortOption
import com.closeby.feature.servicelisting.domain.repository.LocationProvider

/**
 * Sorts listings according to [SortOption].
 *
 * NEAREST_FIRST is delegated to the injected [LocationProvider] so this
 * module never duplicates the Location module's distance/sorting engine.
 * HIGHEST_RATED and LOWEST_PRICE are simple in-module comparators since
 * they don't involve location math.
 */
class SortServicesUseCase(
    private val locationProvider: LocationProvider
) {

    suspend operator fun invoke(listings: List<ServiceListing>, sortOption: SortOption): List<ServiceListing> {
        return when (sortOption) {
            SortOption.NEAREST_FIRST -> locationProvider.sortNearestFirst(listings)
            SortOption.HIGHEST_RATED -> listings.sortedByDescending { it.rating }
            SortOption.LOWEST_PRICE -> listings.sortedBy { it.price.amount }
            SortOption.HIGHEST_PRICE -> listings.sortedByDescending { it.price.amount }
            SortOption.NEWEST -> listings.sortedByDescending { it.id }
        }
    }
}
