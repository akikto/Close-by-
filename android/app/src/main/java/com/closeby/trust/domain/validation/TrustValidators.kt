package com.closeby.trust.domain.validation

import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.trust.domain.model.ReviewInput
import com.closeby.trust.domain.model.ReviewerRole

sealed class TrustValidationError(message: String) : Exception(message) {
    data object InvalidRating : TrustValidationError("Rating must be between 1 and 5.")
    data object RequestNotCompleted : TrustValidationError("Reviews are only allowed after completion.")
    data object DuplicateReview : TrustValidationError("You already submitted a review for this request.")
    data object NotRequestParticipant : TrustValidationError("You are not part of this request.")
    data object EmptyBusinessName : TrustValidationError("Business name is required.")
    data object EmptyContactPhone : TrustValidationError("Contact phone is required.")
    data object EmptyReportReason : TrustValidationError("Please select a report reason.")
    data object DescriptionTooLong : TrustValidationError("Description is too long.")
}

object ReviewValidator {
    const val MAX_COMMENT_LENGTH = 500
    const val MAX_DESCRIPTION_LENGTH = 1000

    fun validateRating(value: Int?): Result<Unit> =
        if (value == null || value !in 1..5) Result.failure(TrustValidationError.InvalidRating)
        else Result.success(Unit)

    fun validateReviewInput(input: ReviewInput, role: ReviewerRole): Result<Unit> {
        validateRating(input.overallRating).onFailure { return Result.failure(it) }
        if (role == ReviewerRole.CUSTOMER) {
            input.serviceQuality?.let { validateRating(it).onFailure { return Result.failure(it) } }
        } else {
            input.professionalism?.let { validateRating(it).onFailure { return Result.failure(it) } }
        }
        input.behaviour?.let { validateRating(it).onFailure { return Result.failure(it) } }
        input.reliability?.let { validateRating(it).onFailure { return Result.failure(it) } }
        if (input.comment != null && input.comment.length > MAX_COMMENT_LENGTH) {
            return Result.failure(TrustValidationError.DescriptionTooLong)
        }
        return Result.success(Unit)
    }

    fun validateEligibility(
        requestStatus: ServiceRequestStatus,
        reviewerId: String,
        customerId: String?,
        providerUserId: String?,
        role: ReviewerRole,
        existingReviewForRole: Boolean
    ): Result<Unit> {
        if (requestStatus != ServiceRequestStatus.COMPLETED) {
            return Result.failure(TrustValidationError.RequestNotCompleted)
        }
        if (existingReviewForRole) {
            return Result.failure(TrustValidationError.DuplicateReview)
        }
        val allowed = when (role) {
            ReviewerRole.CUSTOMER -> customerId != null && customerId == reviewerId
            ReviewerRole.PROVIDER -> providerUserId != null && providerUserId == reviewerId
        }
        return if (allowed) Result.success(Unit)
        else Result.failure(TrustValidationError.NotRequestParticipant)
    }
}

object VerificationValidator {
    fun validateSubmission(businessName: String, contactPhone: String, description: String?): Result<Unit> {
        if (businessName.isBlank()) return Result.failure(TrustValidationError.EmptyBusinessName)
        if (contactPhone.isBlank() || contactPhone.none { it.isDigit() }) {
            return Result.failure(TrustValidationError.EmptyContactPhone)
        }
        if (description != null && description.length > ReviewValidator.MAX_DESCRIPTION_LENGTH) {
            return Result.failure(TrustValidationError.DescriptionTooLong)
        }
        return Result.success(Unit)
    }
}

object ReportValidator {
    fun validate(input: com.closeby.trust.domain.model.ReportInput): Result<Unit> {
        if (input.description != null && input.description.length > ReviewValidator.MAX_DESCRIPTION_LENGTH) {
            return Result.failure(TrustValidationError.DescriptionTooLong)
        }
        return Result.success(Unit)
    }
}

object VerificationStatusTransitions {
    fun canTransition(from: com.closeby.trust.domain.model.VerificationStatus, to: com.closeby.trust.domain.model.VerificationStatus): Boolean =
        when (from) {
            com.closeby.trust.domain.model.VerificationStatus.NOT_SUBMITTED -> to == com.closeby.trust.domain.model.VerificationStatus.PENDING
            com.closeby.trust.domain.model.VerificationStatus.PENDING -> to in setOf(
                com.closeby.trust.domain.model.VerificationStatus.APPROVED,
                com.closeby.trust.domain.model.VerificationStatus.REJECTED
            )
            com.closeby.trust.domain.model.VerificationStatus.APPROVED -> to == com.closeby.trust.domain.model.VerificationStatus.SUSPENDED
            com.closeby.trust.domain.model.VerificationStatus.REJECTED -> to == com.closeby.trust.domain.model.VerificationStatus.PENDING
            com.closeby.trust.domain.model.VerificationStatus.SUSPENDED -> to == com.closeby.trust.domain.model.VerificationStatus.APPROVED
        }
}
