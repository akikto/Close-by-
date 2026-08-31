package com.closeby.feature.servicelisting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavedServicesViewModel(
    private val savedRepository: SavedServiceRepository,
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
                val ids = savedRepository.getSavedEntries().map { it.serviceId }
                if (ids.isEmpty()) {
                    _uiState.value = UiState.Empty
                    return@launch
                }
                val listings = ids.mapNotNull { id ->
                    serviceRepository.getServiceById(id).getOrNull()
                }
                _uiState.value = if (listings.isEmpty()) UiState.Empty else UiState.Success(listings)
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.message ?: "Failed to load saved services.")
            }
        }
    }
}
