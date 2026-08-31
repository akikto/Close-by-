package com.closeby.trust.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.trust.domain.model.ReviewInput
import com.closeby.trust.domain.model.ReviewerRole
import com.closeby.trust.domain.repository.TrustRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubmitReviewFormState(
    val overallRating: Int = 0,
    val serviceQuality: Int = 0,
    val behaviour: Int = 0,
    val reliability: Int = 0,
    val professionalism: Int = 0,
    val comment: String = "",
    val serviceTitle: String = "",
    val alreadyReviewed: Boolean = false
)

class SubmitReviewViewModel(
    private val requestId: String,
    private val role: ReviewerRole,
    private val reviewerId: String,
    private val trustRepository: TrustRepository,
    private val serviceRequestRepository: ServiceRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SubmitReviewFormState>>(UiState.Idle)
    val uiState: StateFlow<UiState<SubmitReviewFormState>> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val request = serviceRequestRepository.getRequestById(requestId).getOrElse { error ->
                _uiState.value = UiState.Error(error.message ?: "Request not found.")
                return@launch
            }
            val alreadyReviewed = trustRepository.hasReviewForRequest(requestId, reviewerId, role)
                .getOrDefault(false)
            _uiState.value = UiState.Success(
                SubmitReviewFormState(
                    serviceTitle = request.serviceTitle,
                    alreadyReviewed = alreadyReviewed
                )
            )
        }
    }

    fun updateOverallRating(value: Int) = updateForm { it.copy(overallRating = value) }
    fun updateServiceQuality(value: Int) = updateForm { it.copy(serviceQuality = value) }
    fun updateBehaviour(value: Int) = updateForm { it.copy(behaviour = value) }
    fun updateReliability(value: Int) = updateForm { it.copy(reliability = value) }
    fun updateProfessionalism(value: Int) = updateForm { it.copy(professionalism = value) }
    fun updateComment(value: String) = updateForm { it.copy(comment = value) }

    fun submit() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _submitState.value = UiState.Loading
        viewModelScope.launch {
            trustRepository.submitReview(
                requestId = requestId,
                reviewerId = reviewerId,
                role = role,
                input = ReviewInput(
                    requestId = requestId,
                    overallRating = current.overallRating,
                    serviceQuality = current.serviceQuality.takeIf { it in 1..5 },
                    behaviour = current.behaviour.takeIf { it in 1..5 },
                    reliability = current.reliability.takeIf { it in 1..5 },
                    professionalism = current.professionalism.takeIf { it in 1..5 },
                    comment = current.comment.ifBlank { null }
                )
            ).onSuccess {
                _submitState.value = UiState.Success(Unit)
                load()
            }.onFailure { error ->
                _submitState.value = UiState.Error(error.message ?: "Could not submit review.")
            }
        }
    }

    private fun updateForm(transform: (SubmitReviewFormState) -> SubmitReviewFormState) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(transform(current))
    }
}
