package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.model.AdminDeletionRequestSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDeletionRequestsViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AdminDeletionRequestSummary>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<AdminDeletionRequestSummary>>> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.listAccountDeletionRequests()
                .onSuccess { requests ->
                    _uiState.value = if (requests.isEmpty()) {
                        UiState.Success(emptyList())
                    } else {
                        UiState.Success(requests)
                    }
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load deletion requests.")
                }
        }
    }

    fun approve(requestId: String) {
        viewModelScope.launch {
            repository.approveAccountDeletion(requestId)
                .onSuccess { load() }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Could not approve request.")
                }
        }
    }

    fun reject(requestId: String) {
        viewModelScope.launch {
            repository.rejectAccountDeletion(requestId)
                .onSuccess { load() }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Could not reject request.")
                }
        }
    }
}
