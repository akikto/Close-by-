package com.closeby.request.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.core.session.ClientSessionStorage
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestDetailsViewModel(
    private val requestId: String,
    private val customerId: String?,
    private val providerId: String?,
    private val repository: ServiceRequestRepository,
    private val clientSessionStorage: ClientSessionStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ServiceRequest>>(UiState.Idle)
    val uiState: StateFlow<UiState<ServiceRequest>> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.getRequestById(requestId)
                .onSuccess { request -> _uiState.value = UiState.Success(request) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load request.")
                }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            val sessionId = clientSessionStorage.getOrCreateSessionId()
            repository.cancelRequest(requestId, customerId, sessionId)
                .onSuccess { load() }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Could not cancel.")
                }
        }
    }

    fun accept() {
        val pid = providerId ?: return
        viewModelScope.launch {
            repository.acceptRequest(requestId, pid).onSuccess { load() }
        }
    }

    fun reject() {
        val pid = providerId ?: return
        viewModelScope.launch {
            repository.rejectRequest(requestId, pid).onSuccess { load() }
        }
    }

    fun complete() {
        val pid = providerId ?: return
        viewModelScope.launch {
            repository.completeRequest(requestId, pid).onSuccess { load() }
        }
    }

    fun canCancel(request: ServiceRequest): Boolean =
        request.status == ServiceRequestStatus.PENDING
}
