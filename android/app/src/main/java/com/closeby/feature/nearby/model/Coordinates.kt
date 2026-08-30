package com.closeby.feature.nearby.model

/**
 * Internal representation of a geographic point.
 *
 * IMPORTANT (privacy): [Coordinates] values for the *current user* are internal
 * application data used only for nearby-distance calculations. They must never be
 * rendered directly in any public-facing UI. Only derived, relative information
 * (e.g. "2.3 km away") may be shown to other users.
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { "latitude must be in [-90, 90], was $latitude" }
        require(longitude in -180.0..180.0) { "longitude must be in [-180, 180], was $longitude" }
    }
}
