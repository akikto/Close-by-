package com.closeby.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportStatus
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminReportsViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Report>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Report>>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.listReports()
                .onSuccess { reports -> _uiState.value = UiState.Success(reports) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load reports.")
                }
        }
    }

    fun updateStatus(reportId: String, status: ReportStatus) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status, note = null)
                .onSuccess {
                    _actionMessage.value = "Report updated"
                    load()
                }
                .onFailure { error ->
                    _actionMessage.value = error.message ?: "Could not update report."
                }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }
}
