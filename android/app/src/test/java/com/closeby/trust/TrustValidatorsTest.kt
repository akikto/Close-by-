package com.closeby.trust

import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.trust.domain.model.ReviewerRole
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.trust.domain.validation.ReviewValidator
import com.closeby.trust.domain.validation.TrustValidationError
import com.closeby.trust.domain.validation.VerificationStatusTransitions
import com.closeby.trust.domain.validation.VerificationValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustValidatorsTest {

    @Test
    fun verificationStatusTransitions_followExpectedPaths() {
        assertTrue(
            VerificationStatusTransitions.canTransition(
                VerificationStatus.NOT_SUBMITTED,
                VerificationStatus.PENDING
            )
        )
        assertTrue(
            VerificationStatusTransitions.canTransition(
                VerificationStatus.PENDING,
                VerificationStatus.APPROVED
            )
        )
        assertTrue(
            VerificationStatusTransitions.canTransition(
                VerificationStatus.PENDING,
                VerificationStatus.REJECTED
            )
        )
        assertFalse(
            VerificationStatusTransitions.canTransition(
                VerificationStatus.NOT_SUBMITTED,
                VerificationStatus.APPROVED
            )
        )
        assertTrue(
            VerificationStatusTransitions.canTransition(
                VerificationStatus.REJECTED,
                VerificationStatus.PENDING
            )
        )
    }

    @Test
    fun ratingMustBeBetweenOneAndFive() {
        assertTrue(ReviewValidator.validateRating(3).isSuccess)
        assertTrue(ReviewValidator.validateRating(null).isFailure)
        assertTrue(ReviewValidator.validateRating(0).isFailure)
        assertTrue(ReviewValidator.validateRating(6).isFailure)
    }

    @Test
    fun reviewEligibilityRequiresCompletedRequestAndParticipant() {
        val result = ReviewValidator.validateEligibility(
            requestStatus = ServiceRequestStatus.COMPLETED,
            reviewerId = "customer-1",
            customerId = "customer-1",
            providerUserId = "provider-1",
            role = ReviewerRole.CUSTOMER,
            existingReviewForRole = false
        )
        assertTrue(result.isSuccess)

        val notCompleted = ReviewValidator.validateEligibility(
            requestStatus = ServiceRequestStatus.ACCEPTED,
            reviewerId = "customer-1",
            customerId = "customer-1",
            providerUserId = "provider-1",
            role = ReviewerRole.CUSTOMER,
            existingReviewForRole = false
        )
        assertTrue(notCompleted.isFailure)
        assertEquals(TrustValidationError.RequestNotCompleted, notCompleted.exceptionOrNull())
    }

    @Test
    fun duplicateReviewIsRejected() {
        val result = ReviewValidator.validateEligibility(
            requestStatus = ServiceRequestStatus.COMPLETED,
            reviewerId = "customer-1",
            customerId = "customer-1",
            providerUserId = "provider-1",
            role = ReviewerRole.CUSTOMER,
            existingReviewForRole = true
        )
        assertTrue(result.isFailure)
        assertEquals(TrustValidationError.DuplicateReview, result.exceptionOrNull())
    }

    @Test
    fun verificationSubmissionRequiresBusinessNameAndPhone() {
        assertTrue(VerificationValidator.validateSubmission("Acme", "+919876543210", null).isSuccess)
        assertTrue(VerificationValidator.validateSubmission("", "+919876543210", null).isFailure)
        assertTrue(VerificationValidator.validateSubmission("Acme", "phone", null).isFailure)
    }
}
