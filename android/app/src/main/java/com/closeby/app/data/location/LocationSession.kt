package com.closeby.app.data.location

import com.closeby.feature.nearby.location.LocationAvailability
import com.closeby.feature.nearby.location.LocationProvider as DeviceLocationProvider
import com.closeby.feature.nearby.model.Coordinates
import com.closeby.feature.servicelisting.domain.model.LocationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Bridges the nearby module's device [DeviceLocationProvider] into flows
 * consumed by the servicelisting layer. Never exposes raw coordinates to UI.
 */
class LocationSession(
    private val deviceLocation: DeviceLocationProvider
) {
    private val _coordinates = MutableStateFlow<Coordinates?>(null)
    val coordinates: StateFlow<Coordinates?> = _coordinates.asStateFlow()

    private val _status = MutableStateFlow(LocationStatus.UNAVAILABLE)
    val status: StateFlow<LocationStatus> = _status.asStateFlow()

    private var started = false

    fun bind(scope: CoroutineScope) {
        if (started) return
        started = true
        scope.launch {
            deviceLocation.observeLocation().collect { availability ->
                when (availability) {
                    is LocationAvailability.Loading ->
                        _status.value = LocationStatus.UNAVAILABLE
                    is LocationAvailability.Available -> {
                        _coordinates.value = availability.coordinates
                        _status.value = LocationStatus.AVAILABLE
                    }
                    is LocationAvailability.PermissionDenied ->
                        _status.value = LocationStatus.PERMISSION_DENIED
                    is LocationAvailability.PermissionPermanentlyDenied ->
                        _status.value = LocationStatus.PERMISSION_DENIED
                    is LocationAvailability.GpsDisabled ->
                        _status.value = LocationStatus.LOCATION_DISABLED
                    is LocationAvailability.Unavailable,
                    is LocationAvailability.NetworkUnavailable ->
                        _status.value = LocationStatus.UNAVAILABLE
                    is LocationAvailability.Error ->
                        _status.value = LocationStatus.UNAVAILABLE
                }
            }
        }
    }

    fun retry() {
        deviceLocation.retry()
    }

    fun isDeviceLocationEnabled(): Boolean = deviceLocation.isLocationServiceEnabled()
}
