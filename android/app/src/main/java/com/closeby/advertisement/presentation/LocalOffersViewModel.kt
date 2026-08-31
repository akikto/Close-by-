package com.closeby.advertisement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.advertisement.domain.GeoTargeting
import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.advertisement.domain.repository.AdvertisementRepository
import com.closeby.app.data.location.LocationSession
import com.closeby.feature.servicelisting.domain.model.LocationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class LocalOfferItem(
    val ad: Advertisement,
    val distanceLabel: String
)

sealed class LocalOffersUiState {
    data object Loading : LocalOffersUiState()
    data object LocationUnavailable : LocalOffersUiState()
    data class Ready(val offers: List<LocalOfferItem>) : LocalOffersUiState()
    data class Error(val message: String) : LocalOffersUiState()
}

class LocalOffersViewModel(
    private val repository: AdvertisementRepository,
    private val locationSession: LocationSession
) : ViewModel() {

    private val _uiState = MutableStateFlow<LocalOffersUiState>(LocalOffersUiState.Loading)
    val uiState: StateFlow<LocalOffersUiState> = _uiState.asStateFlow()

    private var approvedAds: List<Advertisement> = emptyList()

    init {
        locationSession.bind(viewModelScope)
        viewModelScope.launch {
            combine(locationSession.status, locationSession.coordinates) { status, coords ->
                status to coords
            }.collect { (status, coords) ->
                when {
                    status != LocationStatus.AVAILABLE || coords == null ->
                        _uiState.value = LocalOffersUiState.LocationUnavailable
                    approvedAds.isEmpty() ->
                        _uiState.value = LocalOffersUiState.Ready(emptyList())
                    else -> _uiState.value = LocalOffersUiState.Ready(buildOffers(coords.latitude, coords.longitude))
                }
            }
        }
    }

    fun load() {
        _uiState.value = LocalOffersUiState.Loading
        viewModelScope.launch {
            repository.getApprovedAds()
                .onSuccess { ads ->
                    approvedAds = ads
                    val coords = locationSession.coordinates.value
                    val status = locationSession.status.value
                    _uiState.value = when {
                        status != LocationStatus.AVAILABLE || coords == null ->
                            LocalOffersUiState.LocationUnavailable
                        else -> LocalOffersUiState.Ready(
                            buildOffers(coords.latitude, coords.longitude)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = LocalOffersUiState.Error(error.message ?: "Failed to load offers.")
                }
        }
    }

    private fun buildOffers(userLat: Double, userLng: Double): List<LocalOfferItem> {
        val now = System.currentTimeMillis()
        return approvedAds
            .filter { GeoTargeting.isEligible(it, userLat, userLng, now) }
            .map { ad ->
                LocalOfferItem(
                    ad = ad,
                    distanceLabel = GeoTargeting.formatDistanceLabel(ad, userLat, userLng)
                )
            }
            .sortedBy { it.ad.title }
    }
}
