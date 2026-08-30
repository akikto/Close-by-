package com.closeby.availability.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.availability.domain.repository.AvailabilityRepository
import com.closeby.availability.domain.validation.AvailabilityValidator
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvailabilityViewModel(
    private val providerId: String,
    private val repository: AvailabilityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ProviderAvailability>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<ProviderAvailability>>> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.getProviderAvailability(providerId)
                .onSuccess { list -> _uiState.value = UiState.Success(list) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load availability.")
                }
        }
    }

    fun save(entries: List<ProviderAvailability>) {
        AvailabilityValidator.validateAll(entries).onFailure { error ->
            _uiState.value = UiState.Error(error.message ?: "Invalid availability.")
            return
        }

        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.saveAvailability(entries)
                .onSuccess { _uiState.value = UiState.Success(entries) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to save availability.")
                }
        }
    }
}
