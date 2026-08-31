package com.closeby.feature.servicelisting.domain.model

import com.closeby.feature.nearby.util.DistanceFormatter

/**
 * Distance/location data CONSUMED from Agent 2's Location + Nearby module.
 *
 * This module does NOT calculate distance itself. Agent 2 owns:
 *  - device location acquisition
 *  - distance calculation
 *  - radius filtering
 *  - nearest-first sorting math
 *
 * Agent 3 (this module) only receives the result through this contract and
 * renders/filters/sorts using the values supplied here.
 */
data class DistanceInfo(
    val distanceKm: Double?,
    val status: LocationStatus
) {
    /** Display string such as "850 m away" or "2.4 km away", or null when unknown. */
    fun formatted(): String? =
        distanceKm?.let { km ->
            DistanceFormatter.formatWithSuffix(km * 1000.0)
        }
}

enum class LocationStatus {
    AVAILABLE,
    PERMISSION_DENIED,
    LOCATION_DISABLED,
    UNAVAILABLE
}
