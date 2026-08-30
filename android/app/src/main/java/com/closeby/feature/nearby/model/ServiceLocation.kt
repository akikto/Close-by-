package com.closeby.feature.nearby.model

/**
 * Minimal geographic pointer for a service/provider listing.
 *
 * Deliberately does NOT carry unrelated service fields (name, images, description,
 * pricing, etc.) — those belong to the service-catalog module. This module only needs
 * enough to place a listing on the map / compute a distance from the user.
 */
data class ServiceLocation(
    val serviceId: String,
    val latitude: Double,
    val longitude: Double
) {
    fun toCoordinates(): Coordinates = Coordinates(latitude, longitude)
}
