package com.closeby.trust.data.repository

import com.closeby.app.data.remote.ProviderRemoteDataSource
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.notification.domain.handler.NotificationEventPublisher
import com.closeby.trust.data.mapper.TrustMapper
import com.closeby.trust.data.model.ReportInsertDto
import com.closeby.trust.data.model.ReviewInsertDto
import com.closeby.trust.data.model.UserBlockInsertDto
import com.closeby.trust.data.model.VerificationInsertDto
import com.closeby.trust.data.remote.BlockRemoteDataSource
import com.closeby.trust.data.remote.ReportRemoteDataSource
import com.closeby.trust.data.remote.ReviewRemoteDataSource
import com.closeby.trust.data.remote.VerificationRemoteDataSource
import com.closeby.trust.domain.model.ReportInput
import com.closeby.trust.domain.model.ReviewInput
import com.closeby.trust.domain.model.ReviewerRole
import com.closeby.trust.domain.model.VerificationInput
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.trust.domain.repository.TrustRepository
import com.closeby.trust.domain.validation.ReportValidator
import com.closeby.trust.domain.validation.ReviewValidator
import com.closeby.trust.domain.validation.VerificationValidator

class SupabaseTrustRepository(
    private val verificationRemote: VerificationRemoteDataSource = VerificationRemoteDataSource(),
    private val reviewRemote: ReviewRemoteDataSource = ReviewRemoteDataSource(),
    private val reportRemote: ReportRemoteDataSource = ReportRemoteDataSource(),
    private val blockRemote: BlockRemoteDataSource = BlockRemoteDataSource(),
    private val providerRemote: ProviderRemoteDataSource = ProviderRemoteDataSource(),
    private val serviceRequestRepository: ServiceRequestRepository
) : TrustRepository {

    override suspend fun submitVerification(
        providerId: String,
        submittedBy: String,
        input: VerificationInput
    ) = runCatching {
        VerificationValidator.validateSubmission(
            input.businessName,
            input.contactPhone,
            input.description
        ).getOrThrow()

        val dto = verificationRemote.insert(
            VerificationInsertDto(
                providerId = providerId,
                submittedBy = submittedBy,
                businessName = input.businessName.trim(),
                contactPhone = input.contactPhone.trim(),
                description = input.description?.trim(),
                documentUrl = input.documentUrl?.trim()
            )
        )
        TrustMapper.toDomain(dto)?.also {
            NotificationEventPublisher.verificationSubmitted(submittedBy, providerId)
        } ?: throw IllegalStateException("Verification has invalid data.")
    }

    override suspend fun getLatestVerificationSubmission(providerId: String) = runCatching {
        verificationRemote.getLatestByProvider(providerId)?.let(TrustMapper::toDomain)
    }

    override suspend fun getVerificationStatus(providerId: String) = runCatching {
        val provider = providerRemote.getProviderById(providerId)
            ?: throw NoSuchElementException("Provider not found.")
        if (provider.verificationStatus != null) {
            VerificationStatus.fromRaw(provider.verificationStatus)
        } else if (provider.isVerified) {
            VerificationStatus.APPROVED
        } else {
            VerificationStatus.NOT_SUBMITTED
        }
    }

    override suspend fun submitReview(
        requestId: String,
        reviewerId: String,
        role: ReviewerRole,
        input: ReviewInput
    ) = runCatching {
        ReviewValidator.validateReviewInput(input, role).getOrThrow()

        val request = serviceRequestRepository.getRequestById(requestId).getOrThrow()
        val provider = providerRemote.getProviderById(request.providerId)
        val providerUserId = provider?.userId

        val existing = reviewRemote.existsForRequest(requestId, reviewerId, role.name)
        ReviewValidator.validateEligibility(
            requestStatus = request.status,
            reviewerId = reviewerId,
            customerId = request.customerId,
            providerUserId = providerUserId,
            role = role,
            existingReviewForRole = existing
        ).getOrThrow()

        val revieweeId = when (role) {
            ReviewerRole.CUSTOMER -> providerUserId
                ?: throw IllegalStateException("Provider account not linked.")
            ReviewerRole.PROVIDER -> request.customerId
                ?: throw IllegalStateException("Customer account not linked.")
        }

        val dto = reviewRemote.insert(
            ReviewInsertDto(
                requestId = requestId,
                serviceId = request.serviceId,
                providerId = request.providerId,
                customerId = request.customerId,
                reviewerId = reviewerId,
                revieweeId = revieweeId,
                reviewerRole = role.name,
                overallRating = input.overallRating,
                serviceQuality = input.serviceQuality,
                behaviour = input.behaviour,
                reliability = input.reliability,
                professionalism = input.professionalism,
                comment = input.comment?.trim()
            )
        )
        TrustMapper.toDomain(dto) ?: throw IllegalStateException("Review has invalid data.")
    }

    override suspend fun getReviewsForProvider(providerId: String) = runCatching {
        reviewRemote.getByProvider(providerId).mapNotNull(TrustMapper::toDomain)
    }

    override suspend fun hasReviewForRequest(
        requestId: String,
        reviewerId: String,
        role: ReviewerRole
    ) = runCatching {
        reviewRemote.existsForRequest(requestId, reviewerId, role.name)
    }

    override suspend fun submitReport(reporterId: String, input: ReportInput) = runCatching {
        ReportValidator.validate(input).getOrThrow()
        val dto = reportRemote.insert(
            ReportInsertDto(
                reporterId = reporterId,
                targetType = input.targetType.name,
                targetId = input.targetId,
                reason = input.reason.name,
                description = input.description?.trim()
            )
        )
        TrustMapper.toDomain(dto) ?: throw IllegalStateException("Report has invalid data.")
    }

    override suspend fun blockProvider(blockerId: String, providerId: String) = runCatching {
        val dto = blockRemote.insert(
            UserBlockInsertDto(
                blockerId = blockerId,
                blockedProviderId = providerId
            )
        )
        TrustMapper.toDomain(dto)
    }

    override suspend fun unblockProvider(blockerId: String, providerId: String) = runCatching {
        blockRemote.deleteProviderBlock(blockerId, providerId)
    }

    override suspend fun getBlockedProviderIds(blockerId: String) = runCatching {
        blockRemote.getByBlocker(blockerId)
            .mapNotNull { it.blockedProviderId }
            .toSet()
    }

    override suspend fun isProviderBlocked(blockerId: String, providerId: String) = runCatching {
        blockRemote.getByBlocker(blockerId)
            .any { it.blockedProviderId == providerId }
    }
}
