package com.closeby.trust.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.trust.domain.model.VerificationInput
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.trust.domain.model.VerificationSubmission
import com.closeby.trust.domain.repository.TrustRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VerificationFormState(
    val businessName: String = "",
    val contactPhone: String = "",
    val description: String = "",
    val documentUrl: String = "",
    val status: VerificationStatus = VerificationStatus.NOT_SUBMITTED,
    val latestSubmission: VerificationSubmission? = null
)

class VerificationViewModel(
    private val providerId: String,
    private val submittedBy: String,
    private val trustRepository: TrustRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<VerificationFormState>>(UiState.Idle)
    val uiState: StateFlow<UiState<VerificationFormState>> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val status = trustRepository.getVerificationStatus(providerId).getOrElse {
                VerificationStatus.NOT_SUBMITTED
            }
            val latest = trustRepository.getLatestVerificationSubmission(providerId).getOrNull()
            _uiState.value = UiState.Success(
                VerificationFormState(
                    businessName = latest?.businessName.orEmpty(),
                    contactPhone = latest?.contactPhone.orEmpty(),
                    description = latest?.description.orEmpty(),
                    documentUrl = latest?.documentUrl.orEmpty(),
                    status = status,
                    latestSubmission = latest
                )
            )
        }
    }

    fun updateBusinessName(value: String) = updateForm { it.copy(businessName = value) }
    fun updateContactPhone(value: String) = updateForm { it.copy(contactPhone = value) }
    fun updateDescription(value: String) = updateForm { it.copy(description = value) }
    fun updateDocumentUrl(value: String) = updateForm { it.copy(documentUrl = value) }

    fun submit() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _submitState.value = UiState.Loading
        viewModelScope.launch {
            trustRepository.submitVerification(
                providerId = providerId,
                submittedBy = submittedBy,
                input = VerificationInput(
                    businessName = current.businessName,
                    contactPhone = current.contactPhone,
                    description = current.description.ifBlank { null },
                    documentUrl = current.documentUrl.ifBlank { null }
                )
            ).onSuccess {
                _submitState.value = UiState.Success(Unit)
                load()
            }.onFailure { error ->
                _submitState.value = UiState.Error(error.message ?: "Submission failed.")
            }
        }
    }

    private fun updateForm(transform: (VerificationFormState) -> VerificationFormState) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(transform(current))
    }
}
