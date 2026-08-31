package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminVerificationViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AdminProviderSummary>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<AdminProviderSummary>>> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(VerificationStatus.PENDING)
    val selectedTab: StateFlow<VerificationStatus> = _selectedTab.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun selectTab(status: VerificationStatus) {
        _selectedTab.value = status
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.listProviders()
                .onSuccess { providers ->
                    val filtered = providers.filter { it.verificationStatus == _selectedTab.value }
                    _uiState.value = UiState.Success(filtered)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load verifications.")
                }
        }
    }

    fun approve(providerId: String) {
        viewModelScope.launch {
            repository.approveVerification(providerId, note = null)
                .onSuccess {
                    _actionMessage.value = "Verification approved"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not approve verification."
                }
        }
    }

    fun reject(providerId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectVerification(providerId, reason)
                .onSuccess {
                    _actionMessage.value = "Verification rejected"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not reject verification."
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
