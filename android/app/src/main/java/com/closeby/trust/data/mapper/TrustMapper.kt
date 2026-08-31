package com.closeby.trust.data.mapper

import com.closeby.trust.data.model.ReportDto
import com.closeby.trust.data.model.ReviewDto
import com.closeby.trust.data.model.UserBlockDto
import com.closeby.trust.data.model.VerificationSubmissionDto
import com.closeby.trust.domain.model.ModerationStatus
import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportReason
import com.closeby.trust.domain.model.ReportStatus
import com.closeby.trust.domain.model.ReportTargetType
import com.closeby.trust.domain.model.Review
import com.closeby.trust.domain.model.ReviewerRole
import com.closeby.trust.domain.model.UserBlock
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.trust.domain.model.VerificationSubmission
import java.time.Instant

object TrustMapper {

    fun toDomain(dto: VerificationSubmissionDto): VerificationSubmission =
        VerificationSubmission(
            id = dto.id,
            providerId = dto.providerId,
            businessName = dto.businessName,
            contactPhone = dto.contactPhone,
            description = dto.description,
            documentUrl = dto.documentUrl,
            status = VerificationStatus.fromRaw(dto.status),
            adminNote = dto.adminNote,
            createdAt = parseInstant(dto.createdAt),
            updatedAt = parseInstant(dto.updatedAt)
        )

    fun toDomain(dto: ReviewDto): Review? {
        val role = parseReviewerRole(dto.reviewerRole) ?: return null
        return Review(
            id = dto.id,
            requestId = dto.requestId,
            serviceId = dto.serviceId,
            providerId = dto.providerId,
            customerId = dto.customerId,
            reviewerId = dto.reviewerId,
            revieweeId = dto.revieweeId,
            reviewerRole = role,
            overallRating = dto.overallRating,
            serviceQuality = dto.serviceQuality,
            behaviour = dto.behaviour,
            reliability = dto.reliability,
            professionalism = dto.professionalism,
            comment = dto.comment,
            moderationStatus = parseModerationStatus(dto.moderationStatus),
            isVisible = dto.isVisible,
            createdAt = parseInstant(dto.createdAt),
            updatedAt = parseInstant(dto.updatedAt)
        )
    }

    fun toDomain(dto: ReportDto): Report? {
        val targetType = parseTargetType(dto.targetType) ?: return null
        val reason = parseReportReason(dto.reason) ?: return null
        val status = parseReportStatus(dto.status) ?: return null
        return Report(
            id = dto.id,
            reporterId = dto.reporterId,
            targetType = targetType,
            targetId = dto.targetId,
            reason = reason,
            description = dto.description,
            status = status,
            createdAt = parseInstant(dto.createdAt),
            resolvedAt = dto.resolvedAt?.let { parseInstant(it) }
        )
    }

    fun toDomain(dto: UserBlockDto): UserBlock =
        UserBlock(
            id = dto.id,
            blockerId = dto.blockerId,
            blockedProviderId = dto.blockedProviderId,
            blockedUserId = dto.blockedUserId,
            createdAt = parseInstant(dto.createdAt)
        )

    private fun parseReviewerRole(raw: String): ReviewerRole? =
        runCatching { ReviewerRole.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseModerationStatus(raw: String): ModerationStatus =
        runCatching { ModerationStatus.valueOf(raw.trim().uppercase()) }
            .getOrDefault(ModerationStatus.VISIBLE)

    private fun parseTargetType(raw: String): ReportTargetType? =
        runCatching { ReportTargetType.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseReportReason(raw: String): ReportReason? =
        runCatching { ReportReason.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseReportStatus(raw: String): ReportStatus? =
        runCatching { ReportStatus.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseInstant(raw: String): Long =
        Instant.parse(raw).toEpochMilli()
}
