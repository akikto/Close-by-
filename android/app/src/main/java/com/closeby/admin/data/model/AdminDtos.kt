package com.closeby.admin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminDashboardStatsDto(
    @SerialName("total_users") val totalUsers: Int = 0,
    @SerialName("total_providers") val totalProviders: Int = 0,
    @SerialName("active_services") val activeServices: Int = 0,
    @SerialName("pending_verifications") val pendingVerifications: Int = 0,
    @SerialName("pending_advertisements") val pendingAdvertisements: Int = 0,
    @SerialName("open_reports") val openReports: Int = 0,
    @SerialName("pending_deletion_requests") val pendingDeletionRequests: Int = 0
)

@Serializable
data class UserProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("is_suspended") val isSuspended: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UserProfileSuspendUpdateDto(
    @SerialName("is_suspended") val isSuspended: Boolean,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ProviderAdminDto(
    val id: String,
    val name: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("verification_status") val verificationStatus: String = "NOT_SUBMITTED",
    @SerialName("is_suspended") val isSuspended: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0
)

@Serializable
data class ProviderVerificationUpdateDto(
    @SerialName("verification_status") val verificationStatus: String,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("verification_note") val verificationNote: String? = null,
    @SerialName("is_suspended") val isSuspended: Boolean? = null,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class VerificationSubmissionStatusUpdateDto(
    val status: String,
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ServiceAdminDto(
    val id: String,
    @SerialName("provider_id") val providerId: String,
    val title: String,
    val category: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("deleted_at") val deletedAt: String? = null,
    val providers: ServiceProviderNameDto? = null
)

@Serializable
data class ServiceProviderNameDto(
    val name: String? = null
)

@Serializable
data class ServiceActiveAdminUpdateDto(
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ReportStatusUpdateDto(
    val status: String,
    @SerialName("moderation_note") val moderationNote: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class AdvertisementAdminDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("business_name") val businessName: String,
    val title: String,
    val status: String = "PENDING",
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AdvertisementStatusUpdateDto(
    val status: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("approved_at") val approvedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class AdminAuditLogDto(
    val id: String,
    @SerialName("admin_id") val adminId: String,
    val action: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    val reason: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AdminAuditLogInsertDto(
    @SerialName("admin_id") val adminId: String,
    val action: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    val reason: String? = null
)
