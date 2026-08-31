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
 * @deprecated Use [com.closeby.feature.nearby.location.LocationProvider] for device
 * location and [com.closeby.feature.servicelisting.domain.repository.LocationProvider]
 * for listing distance enrichment via [com.closeby.app.data.location.ServicelistingLocationAdapter].
 */
@Deprecated(
    message = "Use nearby.location.LocationProvider + ServicelistingLocationAdapter",
    replaceWith = ReplaceWith("com.closeby.feature.nearby.location.LocationProvider")
)
interface LocationProvider {

    /** Emits the best-effort last known location, if permission is granted. */
    suspend fun getLastKnownLocation(): GeoPoint?

    /** Emits location updates while collected. Requires location permission. */
    fun observeLocationUpdates(): Flow<GeoPoint>

    /** Whether location permission is currently granted. */
    fun hasLocationPermission(): Boolean
}
