package com.closeby.app.core.location

import kotlinx.coroutines.flow.Flow

/**
 * Simple lat/lng value used across the app. Kept minimal on purpose —
 * this is a base-project contract, not the full location feature.
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

/**
 * Contract for reading the device's current location.
 *
 * The real implementation (backed by FusedLocationProviderClient) and the
 * full "nearby providers" search algorithm are out of scope for the base
 * project and will be built by the location/explore feature work.
 */
interface LocationProvider {

    /** Emits the best-effort last known location, if permission is granted. */
    suspend fun getLastKnownLocation(): GeoPoint?

    /** Emits location updates while collected. Requires location permission. */
    fun observeLocationUpdates(): Flow<GeoPoint>

    /** Whether location permission is currently granted. */
    fun hasLocationPermission(): Boolean
}
