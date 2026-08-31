package com.closeby.advertisement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.advertisement.domain.model.AdStatus
import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.advertisement.domain.repository.AdvertisementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MyAdsUiState {
    data object Loading : MyAdsUiState()
    data class Ready(val ads: List<Advertisement>) : MyAdsUiState()
    data class Error(val message: String) : MyAdsUiState()
}

class MyAdvertisementsViewModel(
    private val ownerId: String,
    private val repository: AdvertisementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyAdsUiState>(MyAdsUiState.Loading)
    val uiState: StateFlow<MyAdsUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = MyAdsUiState.Loading
        viewModelScope.launch {
            repository.getMyAds(ownerId)
                .onSuccess { ads -> _uiState.value = MyAdsUiState.Ready(ads) }
                .onFailure { error ->
                    _uiState.value = MyAdsUiState.Error(error.message ?: "Failed to load ads.")
                }
        }
    }

    fun pauseAd(adId: String) {
        viewModelScope.launch {
            repository.pauseAd(adId, ownerId)
                .onSuccess { load() }
                .onFailure { error ->
                    val current = _uiState.value
                    if (current is MyAdsUiState.Ready) {
                        _uiState.value = MyAdsUiState.Error(error.message ?: "Failed to pause ad.")
                    }
                }
        }
    }

    fun statusLabel(status: AdStatus): String = when (status) {
        AdStatus.PENDING -> "Pending review"
        AdStatus.APPROVED -> "Live"
        AdStatus.REJECTED -> "Rejected"
        AdStatus.PAUSED -> "Paused"
        AdStatus.EXPIRED -> "Expired"
    }
}
