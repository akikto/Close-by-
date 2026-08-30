package com.closeby.feature.nearby.location

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over "get the device's current location". Kept separate from
 * [NearbyServiceRepository][com.closeby.feature.nearby.domain.NearbyServiceRepository]
 * on purpose — this is a device/platform concern, not a data-layer concern.
 *
 * [AndroidLocationProvider] is the real implementation; tests / previews can supply
 * a fake.
 */
interface LocationProvider {
    /** Cold flow of location state; never throws, always emits a state. */
    fun observeLocation(): Flow<LocationAvailability>

    /** One-shot current permission state. */
    fun currentPermissionState(): LocationPermissionState

    /** Whether the device's location service (GPS/network) is currently enabled. */
    fun isLocationServiceEnabled(): Boolean

    /** Ask the flow to re-check/retry after e.g. the user grants permission or enables GPS. */
    fun retry()
}
