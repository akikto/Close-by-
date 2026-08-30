package com.closeby.request.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: ServiceRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<GroupedRequests>>(UiState.Idle)
    val uiState: StateFlow<UiState<GroupedRequests>> = _uiState.asStateFlow()

    fun loadRequests() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.getCustomerRequests(customerId)
                .onSuccess { requests -> _uiState.value = UiState.Success(group(requests)) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load requests.")
                }
        }
    }

    fun cancel(requestId: String) {
        viewModelScope.launch {
            repository.cancelRequest(requestId)
                .onSuccess { loadRequests() }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Could not cancel request.")
                }
        }
    }

    private fun group(requests: List<ServiceRequest>): GroupedRequests = GroupedRequests(
        pending = requests.filter { it.status == ServiceRequestStatus.PENDING },
        accepted = requests.filter { it.status == ServiceRequestStatus.ACCEPTED },
        rejected = requests.filter { it.status == ServiceRequestStatus.REJECTED },
        completed = requests.filter { it.status == ServiceRequestStatus.COMPLETED },
        cancelled = requests.filter { it.status == ServiceRequestStatus.CANCELLED }
    )
}
