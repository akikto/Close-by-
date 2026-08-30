package com.closeby.feature.nearby.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.nearby.location.LocationAvailability
import com.closeby.feature.nearby.location.LocationProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Owns device-location state for the app. Other feature ViewModels (e.g.
 * [NearbySearchViewModel]) observe [locationState] rather than talking to
 * [LocationProvider] directly.
 */
class LocationViewModel(
    private val locationProvider: LocationProvider
) : ViewModel() {

    val locationState: StateFlow<LocationAvailability> = locationProvider
        .observeLocation()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = LocationAvailability.Loading
        )

    /** Call after the user grants permission or enables GPS from a system dialog/settings. */
    fun retry() {
        locationProvider.retry()
    }
}
