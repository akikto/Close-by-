package com.closeby.feature.servicelisting.domain.model

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
    /** Display string such as "1.2 km away", or null when distance is unknown. */
    fun formatted(): String? =
        distanceKm?.let { km ->
            if (km < 1.0) "${(km * 1000).toInt()} m away" else "%.1f km away".format(km)
        }
}

enum class LocationStatus {
    AVAILABLE,
    PERMISSION_DENIED,
    LOCATION_DISABLED,
    UNAVAILABLE
}
