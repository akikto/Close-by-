package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.model.AdminServiceSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminServicesViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AdminServiceSummary>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<AdminServiceSummary>>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.listServices()
                .onSuccess { services -> _uiState.value = UiState.Success(services) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load services.")
                }
        }
    }

    fun toggleService(serviceId: String, enable: Boolean) {
        viewModelScope.launch {
            val result = if (enable) {
                repository.enableService(serviceId, reason = null)
            } else {
                repository.disableService(serviceId, reason = "Disabled by admin")
            }
            result
                .onSuccess {
                    _actionMessage.value = if (enable) "Service enabled" else "Service disabled"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not update service."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }
}
