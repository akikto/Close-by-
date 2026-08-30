package com.closeby.feature.nearby.location

import com.closeby.feature.nearby.model.Coordinates

/**
 * Full lifecycle of "where is the user" as consumed by the ViewModel/UI layer.
 * Designed so the rest of the app never crashes or gets stuck when location is
 * unavailable — every branch is representable and recoverable via [Error.retry].
 */
sealed class LocationAvailability {
    object Loading : LocationAvailability()
    data class Available(val coordinates: Coordinates) : LocationAvailability()
    object PermissionDenied : LocationAvailability()
    object PermissionPermanentlyDenied : LocationAvailability()
    object GpsDisabled : LocationAvailability()
    object Unavailable : LocationAvailability()
    object NetworkUnavailable : LocationAvailability()
    data class Error(val message: String) : LocationAvailability()
}
