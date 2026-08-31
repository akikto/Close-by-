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

data class GroupedRequests(
    val pending: List<ServiceRequest>,
    val accepted: List<ServiceRequest>,
    val rejected: List<ServiceRequest>,
    val completed: List<ServiceRequest>,
    val cancelled: List<ServiceRequest>
)

class CustomerRequestsViewModel(
    private val customerId: String?,
    private val repository: ServiceRequestRepository,
    private val clientSessionStorage: ClientSessionStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<GroupedRequests>>(UiState.Idle)
    val uiState: StateFlow<UiState<GroupedRequests>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun loadRequests() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val sessionId = clientSessionStorage.getOrCreateSessionId()
            repository.getCustomerRequests(customerId, sessionId)
                .onSuccess { requests ->
                    _uiState.value = if (requests.isEmpty()) {
                        UiState.Success(group(emptyList()))
                    } else {
                        UiState.Success(group(requests))
                    }
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load requests.")
                }
        }
    }

    fun cancel(requestId: String) {
        viewModelScope.launch {
            val sessionId = clientSessionStorage.getOrCreateSessionId()
            repository.cancelRequest(requestId, customerId, sessionId)
                .onSuccess {
                    _actionMessage.value = "Request cancelled"
                    loadRequests()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not cancel request."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }

    private fun group(requests: List<ServiceRequest>): GroupedRequests = GroupedRequests(
        pending = requests.filter { it.status == ServiceRequestStatus.PENDING },
        accepted = requests.filter { it.status == ServiceRequestStatus.ACCEPTED },
        rejected = requests.filter { it.status == ServiceRequestStatus.REJECTED },
        completed = requests.filter { it.status == ServiceRequestStatus.COMPLETED },
        cancelled = requests.filter { it.status == ServiceRequestStatus.CANCELLED }
    )
}
