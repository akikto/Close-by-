package com.closeby.feature.nearby.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.nearby.domain.GetNearbyServicesUseCase
import com.closeby.feature.nearby.domain.NearbySearchParams
import com.closeby.feature.nearby.location.LocationAvailability
import com.closeby.feature.nearby.location.LocationProvider
import com.closeby.feature.nearby.model.NearbySearchFilters
import com.closeby.feature.nearby.model.SearchRadius
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the "nearby services" screen: combines device location with the current
 * radius/filters, calls [GetNearbyServicesUseCase], and exposes a single
 * [NearbySearchUiState] for the UI to render.
 *
 * Does not build the screen itself — see the reusable Composables in `ui/` for that.
 */
class NearbySearchViewModel(
    private val locationProvider: LocationProvider,
    private val getNearbyServices: GetNearbyServicesUseCase
) : ViewModel() {

    private val _radius = MutableStateFlow<SearchRadius>(SearchRadius.FiveKm)
    val radius: StateFlow<SearchRadius> = _radius.asStateFlow()

    private val _filters = MutableStateFlow(NearbySearchFilters())
    val filters: StateFlow<NearbySearchFilters> = _filters.asStateFlow()

    private val _uiState = MutableStateFlow<NearbySearchUiState>(NearbySearchUiState.Loading)
    val uiState: StateFlow<NearbySearchUiState> = _uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NearbySearchUiState.Loading)

    init {
        viewModelScope.launch {
            combine(
                locationProvider.observeLocation(),
                _radius,
                _filters
            ) { location, radius, filters -> Triple(location, radius, filters) }
                .collect { (location, radius, filters) ->
                    refresh(location, radius, filters)
                }
        }
    }

    fun setRadius(newRadius: SearchRadius) {
        _radius.value = newRadius
    }

    fun setFilters(newFilters: NearbySearchFilters) {
        _filters.value = newFilters
    }

    fun retryLocation() {
        locationProvider.retry()
    }

    private suspend fun refresh(
        location: LocationAvailability,
        radius: SearchRadius,
        filters: NearbySearchFilters
    ) {
        _uiState.value = when (location) {
            is LocationAvailability.Loading -> NearbySearchUiState.Loading
            is LocationAvailability.PermissionDenied -> NearbySearchUiState.PermissionDenied
            is LocationAvailability.PermissionPermanentlyDenied -> NearbySearchUiState.PermissionPermanentlyDenied
            is LocationAvailability.GpsDisabled -> NearbySearchUiState.GpsDisabled
            is LocationAvailability.Unavailable -> NearbySearchUiState.LocationUnavailable
            is LocationAvailability.NetworkUnavailable -> NearbySearchUiState.NetworkUnavailable
            is LocationAvailability.Error -> NearbySearchUiState.Error(location.message)
            is LocationAvailability.Available -> {
                val params = NearbySearchParams(
                    userLocation = location.coordinates,
                    radiusKm = radius.kilometers,
                    filters = filters
                )
                getNearbyServices(params).fold(
                    onSuccess = { results ->
                        if (results.isEmpty()) NearbySearchUiState.Empty
                        else NearbySearchUiState.Success(results)
                    },
                    onFailure = { t -> NearbySearchUiState.Error(t.message ?: "Search failed") }
                )
            }
        }
    }
}
