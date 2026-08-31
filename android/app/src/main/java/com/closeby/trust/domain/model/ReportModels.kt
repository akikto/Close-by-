package com.closeby.trust.domain.model

enum class ReportTargetType {
    PROVIDER, SERVICE, REVIEW, ADVERTISEMENT, USER
}

enum class ReportReason {
    FAKE_LISTING,
    WRONG_INFO,
    ABUSE,
    SCAM,
    INAPPROPRIATE,
    WRONG_LOCATION,
    OTHER
}

enum class ReportStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    DISMISSED
}

data class Report(
    val id: String,
    val reporterId: String,
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: ReportReason,
    val description: String?,
    val status: ReportStatus,
    val createdAt: Long,
    val resolvedAt: Long?
)

data class ReportInput(
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: ReportReason,
    val description: String?
)

data class UserBlock(
    val id: String,
    val blockerId: String,
    val blockedProviderId: String?,
    val blockedUserId: String?,
    val createdAt: Long
)
