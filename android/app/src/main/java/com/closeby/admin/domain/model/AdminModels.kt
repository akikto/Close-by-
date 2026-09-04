package com.closeby.admin.domain.model

import com.closeby.trust.domain.model.VerificationStatus

data class AdminDashboardStats(
    val totalUsers: Int,
    val totalProviders: Int,
    val activeServices: Int,
    val pendingVerifications: Int,
    val pendingAdvertisements: Int,
    val openReports: Int,
    val pendingDeletionRequests: Int = 0
)

data class AdminDeletionRequestSummary(
    val id: String,
    val userId: String,
    val displayName: String?,
    val reason: String?,
    val status: String,
    val requestedAt: Long,
    val processedAt: Long?
)

data class AdminUserSummary(
    val userId: String,
    val displayName: String?,
    val isSuspended: Boolean,
    val isAdmin: Boolean,
    val createdAt: Long
)

data class AdminProviderSummary(
    val id: String,
    val name: String,
    val userId: String?,
    val verificationStatus: VerificationStatus,
    val isSuspended: Boolean,
    val isActive: Boolean,
    val rating: Double,
    val reviewCount: Int
)

data class AdminServiceSummary(
    val id: String,
    val providerId: String,
    val providerName: String?,
    val title: String,
    val category: String,
    val isActive: Boolean,
    val isDeleted: Boolean
)

data class AdminAdvertisementSummary(
    val id: String,
    val ownerId: String,
    val businessName: String,
    val title: String,
    val status: String,
    val startAt: Long,
    val endAt: Long,
    val createdAt: Long
)

data class AdminAuditLog(
    val id: String,
    val adminId: String,
    val action: AdminAction,
    val targetType: String,
    val targetId: String,
    val reason: String?,
    val createdAt: Long
)

enum class AdminAction {
    SUSPEND_USER,
    UNSUSPEND_USER,
    APPROVE_VERIFICATION,
    REJECT_VERIFICATION,
    SUSPEND_PROVIDER,
    DISABLE_SERVICE,
    ENABLE_SERVICE,
    UPDATE_REPORT_STATUS,
    APPROVE_AD,
    REJECT_AD,
    PAUSE_AD,
    RESUME_AD,
    APPROVE_ACCOUNT_DELETION,
    REJECT_ACCOUNT_DELETION
}

class AdminAccessDeniedException : Exception("Admin access required.")
