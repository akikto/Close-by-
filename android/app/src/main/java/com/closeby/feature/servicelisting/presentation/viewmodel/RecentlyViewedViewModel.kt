package com.closeby.feature.servicelisting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.repository.RecentlyViewedRepository
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecentlyViewedViewModel(
    private val historyRepository: RecentlyViewedRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data object Empty : UiState
        data class Success(val listings: List<ServiceListing>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                val entries = historyRepository.getRecentlyViewed()
                if (entries.isEmpty()) {
                    _uiState.value = UiState.Empty
                    return@launch
                }
                val listings = entries.mapNotNull { entry ->
                    serviceRepository.getServiceById(entry.serviceId).getOrNull()
                }
                _uiState.value = if (listings.isEmpty()) UiState.Empty else UiState.Success(listings)
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.message ?: "Failed to load history.")
            }
        }
    }
}
