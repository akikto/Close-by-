package com.closeby.trust.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.trust.domain.model.ReportInput
import com.closeby.trust.domain.model.ReportReason
import com.closeby.trust.domain.model.ReportTargetType
import com.closeby.trust.domain.repository.TrustRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportFormState(
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: ReportReason? = null,
    val description: String = ""
)

class ReportViewModel(
    private val reporterId: String,
    private val targetType: ReportTargetType,
    private val targetId: String,
    private val trustRepository: TrustRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(
        ReportFormState(targetType = targetType, targetId = targetId)
    )
    val formState: StateFlow<ReportFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun selectReason(reason: ReportReason) {
        _formState.value = _formState.value.copy(reason = reason)
    }

    fun updateDescription(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun submit() {
        val form = _formState.value
        val reason = form.reason ?: run {
            _submitState.value = UiState.Error("Please select a reason.")
            return
        }
        _submitState.value = UiState.Loading
        viewModelScope.launch {
            trustRepository.submitReport(
                reporterId = reporterId,
                input = ReportInput(
                    targetType = form.targetType,
                    targetId = form.targetId,
                    reason = reason,
                    description = form.description.ifBlank { null }
                )
            ).onSuccess {
                _submitState.value = UiState.Success(Unit)
            }.onFailure { error ->
                _submitState.value = UiState.Error(error.message ?: "Could not submit report.")
            }
        }
    }
}
