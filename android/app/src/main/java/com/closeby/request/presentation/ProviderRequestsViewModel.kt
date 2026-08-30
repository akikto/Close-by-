package com.closeby.request.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProviderRequestsViewModel(
    private val providerId: String,
    private val repository: ServiceRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ServiceRequest>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<ServiceRequest>>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun loadRequests() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.getProviderRequests(providerId)
                .onSuccess { requests -> _uiState.value = UiState.Success(requests) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load requests.")
                }
        }
    }

    fun accept(requestId: String) {
        viewModelScope.launch {
            repository.acceptRequest(requestId, providerId)
                .onSuccess {
                    _actionMessage.value = "Request accepted"
                    loadRequests()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not accept request."
                }
        }
    }

    fun reject(requestId: String) {
        viewModelScope.launch {
            repository.rejectRequest(requestId, providerId)
                .onSuccess {
                    _actionMessage.value = "Request rejected"
                    loadRequests()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not reject request."
                }
        }
    }

    fun complete(requestId: String) {
        viewModelScope.launch {
            repository.completeRequest(requestId)
                .onSuccess { loadRequests() }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not complete request."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }
}
