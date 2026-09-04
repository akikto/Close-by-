package com.closeby.app.core.location

import android.content.Context
import com.closeby.feature.nearby.location.AndroidLocationProvider
import com.closeby.feature.nearby.location.LocationAvailability
import com.closeby.feature.nearby.location.LocationPermissionState
import com.closeby.feature.nearby.model.Coordinates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads the device's current GPS coordinates through the canonical nearby location stack.
 * Returns null when permission, GPS, or a fix is unavailable.
 */
object DeviceCoordinatesReader {

    suspend fun readCurrent(context: Context, timeoutMs: Long = 8_000L): Coordinates? {
        val provider = AndroidLocationProvider(context.applicationContext)
        if (!provider.isLocationServiceEnabled()) return null
        when (provider.currentPermissionState()) {
            LocationPermissionState.Denied,
            LocationPermissionState.PermanentlyDenied -> return null
            else -> Unit
        }
        val availability = withTimeoutOrNull(timeoutMs) {
            provider.observeLocation().first { it !is LocationAvailability.Loading }
        } ?: return null
        return (availability as? LocationAvailability.Available)?.coordinates
    }
}
