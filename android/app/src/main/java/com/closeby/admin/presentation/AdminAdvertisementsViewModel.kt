package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.model.AdminAdvertisementSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminAdvertisementsViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AdminAdvertisementSummary>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<AdminAdvertisementSummary>>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.listAdvertisements()
                .onSuccess { ads -> _uiState.value = UiState.Success(ads) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load advertisements.")
                }
        }
    }

    fun approve(adId: String) {
        viewModelScope.launch {
            repository.approveAd(adId, note = null)
                .onSuccess {
                    _actionMessage.value = "Advertisement approved"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not approve ad."
                }
        }
    }

    fun reject(adId: String) {
        viewModelScope.launch {
            repository.rejectAd(adId, reason = "Rejected by admin")
                .onSuccess {
                    _actionMessage.value = "Advertisement rejected"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not reject ad."
                }
        }
    }

    fun pause(adId: String) {
        viewModelScope.launch {
            repository.pauseAd(adId, reason = "Paused by admin")
                .onSuccess {
                    _actionMessage.value = "Advertisement paused"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not pause ad."
                }
        }
    }

    fun resume(adId: String) {
        viewModelScope.launch {
            repository.resumeAd(adId, reason = null)
                .onSuccess {
                    _actionMessage.value = "Advertisement resumed"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not resume ad."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }
}
