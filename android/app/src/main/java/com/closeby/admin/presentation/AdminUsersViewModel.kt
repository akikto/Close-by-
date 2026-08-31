package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.model.AdminUserSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AdminUserSummary>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<AdminUserSummary>>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.listUsers()
                .onSuccess { users -> _uiState.value = UiState.Success(users) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load users.")
                }
        }
    }

    fun toggleSuspend(userId: String, suspend: Boolean) {
        viewModelScope.launch {
            val result = if (suspend) {
                repository.suspendUser(userId, reason = "Suspended by admin")
            } else {
                repository.unsuspendUser(userId, reason = null)
            }
            result
                .onSuccess {
                    _actionMessage.value = if (suspend) "User suspended" else "User unsuspended"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not update user."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }
}
