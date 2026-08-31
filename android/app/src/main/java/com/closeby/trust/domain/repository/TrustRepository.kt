package com.closeby.trust.domain.repository

import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportInput
import com.closeby.trust.domain.model.Review
import com.closeby.trust.domain.model.ReviewInput
import com.closeby.trust.domain.model.ReviewerRole
import com.closeby.trust.domain.model.UserBlock
import com.closeby.trust.domain.model.VerificationInput
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.trust.domain.model.VerificationSubmission

interface TrustRepository {

    suspend fun submitVerification(
        providerId: String,
        submittedBy: String,
        input: VerificationInput
    ): Result<VerificationSubmission>

    suspend fun getLatestVerificationSubmission(providerId: String): Result<VerificationSubmission?>

    suspend fun getVerificationStatus(providerId: String): Result<VerificationStatus>

    suspend fun submitReview(
        requestId: String,
        reviewerId: String,
        role: ReviewerRole,
        input: ReviewInput
    ): Result<Review>

    suspend fun getReviewsForProvider(providerId: String): Result<List<Review>>

    suspend fun hasReviewForRequest(
        requestId: String,
        reviewerId: String,
        role: ReviewerRole
    ): Result<Boolean>

    suspend fun submitReport(reporterId: String, input: ReportInput): Result<Report>

    suspend fun blockProvider(blockerId: String, providerId: String): Result<UserBlock>

    suspend fun unblockProvider(blockerId: String, providerId: String): Result<Unit>

    suspend fun getBlockedProviderIds(blockerId: String): Result<Set<String>>

    suspend fun isProviderBlocked(blockerId: String, providerId: String): Result<Boolean>
}
