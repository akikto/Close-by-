package com.closeby.feature.nearby.domain

import com.closeby.feature.nearby.util.DistanceFormatter

/**
 * Output item of a nearby search: just enough to sort/display by distance.
 * Full service details (name, images, price, etc.) live in the service-catalog
 * module and should be joined in by [serviceId] at a higher layer.
 */
data class NearbyServiceResult(
    val serviceId: String,
    val distanceMeters: Double
) {
    val distanceKilometers: Double get() = distanceMeters / 1000.0

    /** e.g. "2.4 km away" — safe for direct UI display. */
    val distanceLabel: String get() = DistanceFormatter.formatWithSuffix(distanceMeters)
}
