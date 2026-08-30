package com.closeby.feature.nearby.domain

import com.closeby.feature.nearby.model.Coordinates
import com.closeby.feature.nearby.model.NearbySearchFilters

/** Input to [GetNearbyServicesUseCase]. */
data class NearbySearchParams(
    val userLocation: Coordinates,
    val radiusKm: Double,
    val filters: NearbySearchFilters = NearbySearchFilters()
) {
    init {
        require(radiusKm > 0.0) { "radiusKm must be > 0, was $radiusKm" }
    }
}
