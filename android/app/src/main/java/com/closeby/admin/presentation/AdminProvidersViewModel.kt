package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminProvidersViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AdminProviderSummary>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<AdminProviderSummary>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun updateSearch(query: String) {
        _searchQuery.value = query
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val query = _searchQuery.value.takeIf { it.isNotBlank() }
            repository.listProviders(query)
                .onSuccess { providers -> _uiState.value = UiState.Success(providers) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load providers.")
                }
        }
    }

    fun suspendProvider(providerId: String) {
        viewModelScope.launch {
            repository.suspendProvider(providerId, reason = "Suspended by admin")
                .onSuccess {
                    _actionMessage.value = "Provider suspended"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not suspend provider."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }
}
