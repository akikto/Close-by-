package com.closeby.feature.nearby.model

/**
 * Filter criteria for a nearby search. All fields optional/nullable — absence means
 * "no constraint". Passed down to the repository layer so the eventual (Supabase-backed)
 * data source can apply them at the query level.
 *
 * NOTE: [minRating] and [availableOnly] and price-related fields are informational
 * filters only. This module does not implement booking, payment, or availability logic
 * itself — it only models the filter shape so the search contract is stable.
 */
data class NearbySearchFilters(
    val category: String? = null,
    val query: String? = null,
    val availableOnly: Boolean? = null,
    val minRating: Float? = null,
    val maxPrice: Double? = null
)
