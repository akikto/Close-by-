package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.model.AdminDashboardStats
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminGateUiState {
    data object Checking : AdminGateUiState()
    data object Authorized : AdminGateUiState()
    data class Denied(val message: String) : AdminGateUiState()
}

class AdminGateViewModel(
    private val userId: String,
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminGateUiState>(AdminGateUiState.Checking)
    val uiState: StateFlow<AdminGateUiState> = _uiState.asStateFlow()

    init {
        checkAccess()
    }

    fun checkAccess() {
        _uiState.value = AdminGateUiState.Checking
        viewModelScope.launch {
            val isAdmin = repository.isAdmin(userId)
            _uiState.value = if (isAdmin) {
                AdminGateUiState.Authorized
            } else {
                AdminGateUiState.Denied("You do not have admin access.")
            }
        }
    }
}

class AdminDashboardViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AdminDashboardStats>>(UiState.Idle)
    val uiState: StateFlow<UiState<AdminDashboardStats>> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.getDashboardStats()
                .onSuccess { stats -> _uiState.value = UiState.Success(stats) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load dashboard.")
                }
        }
    }
}
