package com.closeby.feature.servicelisting.domain.usecase

import com.closeby.feature.servicelisting.domain.model.ServiceListing

/**
 * Filters a list of listings by a free-text search query.
 * Matches against title, category, subcategory, and provider name.
 * Never hardcodes results — always operates on the list it is given.
 */
class SearchServicesUseCase {

    operator fun invoke(listings: List<ServiceListing>, query: String): List<ServiceListing> {
        if (query.isBlank()) return listings
        val normalized = query.trim().lowercase()
        return listings.filter { listing ->
            listing.title.lowercase().contains(normalized) ||
                listing.category.displayName.lowercase().contains(normalized) ||
                listing.subcategory.displayName.lowercase().contains(normalized) ||
                listing.providerName.lowercase().contains(normalized) ||
                listing.description.lowercase().contains(normalized)
        }
    }
}
