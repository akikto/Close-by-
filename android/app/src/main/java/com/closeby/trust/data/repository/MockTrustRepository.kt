package com.closeby.trust.data.repository

import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportInput
import com.closeby.trust.domain.model.ReportStatus
import com.closeby.trust.domain.model.Review
import com.closeby.trust.domain.model.ReviewInput
import com.closeby.trust.domain.model.ReviewerRole
import com.closeby.trust.domain.model.UserBlock
import com.closeby.trust.domain.model.VerificationInput
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.trust.domain.model.VerificationSubmission
import com.closeby.trust.domain.repository.TrustRepository
import com.closeby.trust.domain.validation.ReportValidator
import com.closeby.trust.domain.validation.ReviewValidator
import com.closeby.trust.domain.validation.VerificationValidator
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class MockTrustRepository(
    private val serviceRequestRepository: ServiceRequestRepository
) : TrustRepository {

    private val verificationSubmissions = CopyOnWriteArrayList<VerificationSubmission>()
    private val verificationStatusByProvider = ConcurrentHashMap<String, VerificationStatus>()
    private val reviews = CopyOnWriteArrayList<Review>()
    private val reports = CopyOnWriteArrayList<Report>()
    private val blocks = CopyOnWriteArrayList<UserBlock>()

    private val demoProviderUserId = "demo-provider-user"
    private val demoCustomerUserId = "demo-customer-user"

    override suspend fun submitVerification(
        providerId: String,
        submittedBy: String,
        input: VerificationInput
    ): Result<VerificationSubmission> = runCatching {
        VerificationValidator.validateSubmission(
            input.businessName,
            input.contactPhone,
            input.description
        ).getOrThrow()

        val now = System.currentTimeMillis()
        val submission = VerificationSubmission(
            id = UUID.randomUUID().toString(),
            providerId = providerId,
            businessName = input.businessName.trim(),
            contactPhone = input.contactPhone.trim(),
            description = input.description?.trim(),
            documentUrl = input.documentUrl?.trim(),
            status = VerificationStatus.PENDING,
            adminNote = null,
            createdAt = now,
            updatedAt = now
        )
        verificationSubmissions.add(submission)
        verificationStatusByProvider[providerId] = VerificationStatus.PENDING
        submission
    }

    override suspend fun getLatestVerificationSubmission(providerId: String): Result<VerificationSubmission?> =
        Result.success(
            verificationSubmissions
                .filter { it.providerId == providerId }
                .maxByOrNull { it.createdAt }
        )

    override suspend fun getVerificationStatus(providerId: String): Result<VerificationStatus> =
        Result.success(verificationStatusByProvider.getOrDefault(providerId, VerificationStatus.NOT_SUBMITTED))

    override suspend fun submitReview(
        requestId: String,
        reviewerId: String,
        role: ReviewerRole,
        input: ReviewInput
    ): Result<Review> = runCatching {
        ReviewValidator.validateReviewInput(input, role).getOrThrow()

        val request = serviceRequestRepository.getRequestById(requestId).getOrThrow()
        val providerUserId = if (request.providerId == "11111111-1111-1111-1111-111111111101") {
            demoProviderUserId
        } else {
            reviewerId.takeIf { role == ReviewerRole.PROVIDER }
        }
        val customerId = request.customerId ?: demoCustomerUserId.takeIf { role == ReviewerRole.CUSTOMER }

        val existing = reviews.any {
            it.requestId == requestId && it.reviewerId == reviewerId && it.reviewerRole == role
        }
        ReviewValidator.validateEligibility(
            requestStatus = request.status,
            reviewerId = reviewerId,
            customerId = customerId,
            providerUserId = providerUserId,
            role = role,
            existingReviewForRole = existing
        ).getOrThrow()

        val revieweeId = when (role) {
            ReviewerRole.CUSTOMER -> providerUserId ?: demoProviderUserId
            ReviewerRole.PROVIDER -> customerId ?: demoCustomerUserId
        }

        val now = System.currentTimeMillis()
        val review = Review(
            id = UUID.randomUUID().toString(),
            requestId = requestId,
            serviceId = request.serviceId,
            providerId = request.providerId,
            customerId = request.customerId,
            reviewerId = reviewerId,
            revieweeId = revieweeId,
            reviewerRole = role,
            overallRating = input.overallRating,
            serviceQuality = input.serviceQuality,
            behaviour = input.behaviour,
            reliability = input.reliability,
            professionalism = input.professionalism,
            comment = input.comment?.trim(),
            createdAt = now,
            updatedAt = now
        )
        reviews.add(review)
        review
    }

    override suspend fun getReviewsForProvider(providerId: String): Result<List<Review>> =
        Result.success(reviews.filter { it.providerId == providerId && it.isVisible })

    override suspend fun hasReviewForRequest(
        requestId: String,
        reviewerId: String,
        role: ReviewerRole
    ): Result<Boolean> = Result.success(
        reviews.any {
            it.requestId == requestId && it.reviewerId == reviewerId && it.reviewerRole == role
        }
    )

    override suspend fun submitReport(reporterId: String, input: ReportInput): Result<Report> = runCatching {
        ReportValidator.validate(input).getOrThrow()
        val now = System.currentTimeMillis()
        val report = Report(
            id = UUID.randomUUID().toString(),
            reporterId = reporterId,
            targetType = input.targetType,
            targetId = input.targetId,
            reason = input.reason,
            description = input.description?.trim(),
            status = ReportStatus.OPEN,
            createdAt = now,
            resolvedAt = null
        )
        reports.add(report)
        report
    }

    override suspend fun blockProvider(blockerId: String, providerId: String): Result<UserBlock> = runCatching {
        blocks.removeIf { it.blockerId == blockerId && it.blockedProviderId == providerId }
        val block = UserBlock(
            id = UUID.randomUUID().toString(),
            blockerId = blockerId,
            blockedProviderId = providerId,
            blockedUserId = null,
            createdAt = System.currentTimeMillis()
        )
        blocks.add(block)
        block
    }

    override suspend fun unblockProvider(blockerId: String, providerId: String): Result<Unit> = runCatching {
        blocks.removeIf { it.blockerId == blockerId && it.blockedProviderId == providerId }
    }

    override suspend fun getBlockedProviderIds(blockerId: String): Result<Set<String>> = Result.success(
        blocks.filter { it.blockerId == blockerId }
            .mapNotNull { it.blockedProviderId }
            .toSet()
    )

    override suspend fun isProviderBlocked(blockerId: String, providerId: String): Result<Boolean> =
        Result.success(
            blocks.any { it.blockerId == blockerId && it.blockedProviderId == providerId }
        )
}
