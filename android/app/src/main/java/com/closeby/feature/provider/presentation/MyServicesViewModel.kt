package com.closeby.feature.provider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.provider.domain.model.ManagedService
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyServicesViewModel(
    private val providerId: String,
    private val repository: ProviderManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ManagedService>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<ManagedService>>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.getMyServices(providerId)
                .onSuccess { services ->
                    _uiState.value = if (services.isEmpty()) {
                        UiState.Success(emptyList())
                    } else {
                        UiState.Success(services)
                    }
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load services.")
                }
        }
    }

    fun toggleActive(serviceId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.setServiceActive(serviceId, providerId, isActive)
                .onSuccess {
                    _actionMessage.value = if (isActive) "Service enabled" else "Service disabled"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not update service."
                }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            repository.deleteService(serviceId, providerId)
                .onSuccess {
                    _actionMessage.value = "Service deleted"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not delete service."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }
}
