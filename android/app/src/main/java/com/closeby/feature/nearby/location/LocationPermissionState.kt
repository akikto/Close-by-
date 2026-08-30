package com.closeby.feature.nearby.location

/** State of the OS location permission, as reported to the UI layer. */
sealed class LocationPermissionState {
    object Unknown : LocationPermissionState()
    object Granted : LocationPermissionState()
    object Denied : LocationPermissionState()
    /** User denied and selected "Don't ask again" — must be resolved via app settings. */
    object PermanentlyDenied : LocationPermissionState()
}
