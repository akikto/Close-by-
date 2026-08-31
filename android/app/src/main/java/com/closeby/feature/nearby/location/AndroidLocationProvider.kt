package com.closeby.feature.nearby.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.closeby.feature.nearby.model.Coordinates
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Real device implementation of [LocationProvider] using the Fused Location Provider.
 *
 * Uses balanced power accuracy. Never throws — always emits [LocationAvailability].
 */
class AndroidLocationProvider(
    private val context: Context
) : LocationProvider {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
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
            LocationPermissionState.Denied
        }
    }

    override fun isLocationServiceEnabled(): Boolean {
        val manager = locationManager ?: return false
        return try {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: SecurityException) {
            false
        }
    }

    override fun retry() {
        retrySignal.tryEmit(Unit)
    }

    override fun observeLocation(): Flow<LocationAvailability> =
        retrySignal.flatMapLatest { singleAttempt() }

    private fun singleAttempt(): Flow<LocationAvailability> = callbackFlow {
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

        val cancellation = CancellationTokenSource()
        try {
            val location = fetchCurrentLocation(cancellation)
            if (location != null) {
                trySend(
                    LocationAvailability.Available(
                        Coordinates(location.latitude, location.longitude)
                    )
                )
            } else {
                trySend(LocationAvailability.Unavailable)
            }
        } catch (_: SecurityException) {
            trySend(LocationAvailability.PermissionDenied)
        } catch (t: Throwable) {
            trySend(LocationAvailability.Error(t.message ?: "Unknown location error"))
        }

        awaitClose {
            cancellation.cancel()
        }
    }

    private suspend fun fetchCurrentLocation(
        cancellation: CancellationTokenSource
    ) = suspendCancellableCoroutine { cont ->
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellation.token
        ).addOnSuccessListener { location ->
            if (cont.isActive) cont.resume(location)
        }.addOnFailureListener { error ->
            if (cont.isActive) cont.resume(null)
        }
        cont.invokeOnCancellation { cancellation.cancel() }
    }
}
