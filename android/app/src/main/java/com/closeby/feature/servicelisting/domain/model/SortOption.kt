package com.closeby.feature.servicelisting.domain.model

/**
 * Sort options for the listing screen.
 *
 * NEAREST_FIRST is the default and requires `distanceInfo.distanceKm` to be
 * supplied by the Location module — this module never computes distance
 * itself, it only sorts using values it already received.
 */
enum class SortOption(val label: String) {
    NEAREST_FIRST("Nearest"),
    HIGHEST_RATED("Rating"),
    LOWEST_PRICE("Price: Low → High"),
    HIGHEST_PRICE("Price: High → Low"),
    NEWEST("Newest");

    companion object {
        val DEFAULT = NEAREST_FIRST
    }
}
