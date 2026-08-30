package com.closeby.feature.nearby.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.closeby.feature.nearby.model.Coordinates
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Real device implementation of [LocationProvider].
 *
 * Uses [LocationManager] directly (no Google Play Services / fused-location
 * dependency) to keep this module dependency-light, as requested. If the base
 * project already depends on Play Services elsewhere, this can be swapped for a
 * FusedLocationProviderClient-backed implementation behind the same interface
 * without touching any calling code.
 *
 * Never throws on missing permission / disabled GPS — always emits a
 * [LocationAvailability] state instead.
 */
class AndroidLocationProvider(
    private val context: Context
) : LocationProvider {

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val retrySignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun currentPermissionState(): LocationPermissionState {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return if (fineGranted || coarseGranted) {
            LocationPermissionState.Granted
        } else {
            // Distinguishing "denied" vs "permanently denied" requires an Activity
            // (shouldShowRequestPermissionRationale). The UI layer (which has the
            // Activity/Compose context) is expected to refine this via
            // LocationPermissionView's own rationale check; this provider reports
            // the conservative "Denied" state here.
            LocationPermissionState.Denied
        }
    }

    override fun isLocationServiceEnabled(): Boolean {
        val manager = locationManager ?: return false
        return try {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (t: SecurityException) {
            false
        }
    }

    override fun retry() {
        retrySignal.tryEmit(Unit)
    }

    /** Re-runs [singleAttempt] every time [retry] is called, restarting the location fix. */
    override fun observeLocation(): Flow<LocationAvailability> =
        retrySignal.flatMapLatest { singleAttempt() }

    private fun singleAttempt(): Flow<LocationAvailability> {
        return callbackFlow {
            val manager = locationManager
            if (manager == null) {
                trySend(LocationAvailability.Unavailable)
                awaitClose { }
                return@callbackFlow
            }

            when (currentPermissionState()) {
                LocationPermissionState.Denied -> {
                    trySend(LocationAvailability.PermissionDenied)
                    awaitClose { }
                    return@callbackFlow
                }
                LocationPermissionState.PermanentlyDenied -> {
                    trySend(LocationAvailability.PermissionPermanentlyDenied)
                    awaitClose { }
                    return@callbackFlow
                }
                else -> Unit
            }

            if (!isLocationServiceEnabled()) {
                trySend(LocationAvailability.GpsDisabled)
                awaitClose { }
                return@callbackFlow
            }

            trySend(LocationAvailability.Loading)

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trySend(
                        LocationAvailability.Available(
                            Coordinates(location.latitude, location.longitude)
                        )
                    )
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) {
                    trySend(LocationAvailability.GpsDisabled)
                }
            }

            try {
                val provider = when {
                    manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                    else -> null
                }

                if (provider == null) {
                    trySend(LocationAvailability.GpsDisabled)
                } else {
                    manager.requestLocationUpdates(
                        provider,
                        MIN_UPDATE_INTERVAL_MS,
                        MIN_UPDATE_DISTANCE_M,
                        listener
                    )
                    // Seed with last-known location, if any, while waiting for a fresh fix.
                    manager.getLastKnownLocation(provider)?.let { last ->
                        trySend(
                            LocationAvailability.Available(
                                Coordinates(last.latitude, last.longitude)
                            )
                        )
                    }
                }
            } catch (e: SecurityException) {
                trySend(LocationAvailability.PermissionDenied)
            } catch (t: Throwable) {
                trySend(LocationAvailability.Error(t.message ?: "Unknown location error"))
            }

            awaitClose {
                try {
                    manager.removeUpdates(listener)
                } catch (t: SecurityException) {
                    // Permission may have been revoked mid-flight; nothing to clean up.
                }
            }
        }
    }

    private companion object {
        const val MIN_UPDATE_INTERVAL_MS = 5_000L
        const val MIN_UPDATE_DISTANCE_M = 10f
    }
}
