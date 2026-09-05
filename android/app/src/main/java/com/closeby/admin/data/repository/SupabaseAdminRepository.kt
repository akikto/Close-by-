package com.closeby.admin.data.repository

import com.closeby.admin.data.mapper.AdminMapper
import com.closeby.admin.data.model.AccountDeletionStatusUpdateDto
import com.closeby.admin.data.model.AdminAuditLogInsertDto
import com.closeby.admin.data.model.AdvertisementStatusUpdateDto
import com.closeby.admin.data.model.ProviderVerificationUpdateDto
import com.closeby.admin.data.model.ReportStatusUpdateDto
import com.closeby.admin.data.remote.AdminRemoteDataSource
import com.closeby.admin.domain.model.AdminAccessDeniedException
import com.closeby.admin.domain.model.AdminAction
import com.closeby.admin.domain.model.AdminAdvertisementSummary
import com.closeby.admin.domain.model.AdminDashboardStats
import com.closeby.admin.domain.model.AdminDeletionRequestSummary
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.admin.domain.model.AdminServiceSummary
import com.closeby.admin.domain.model.AdminUserSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.trust.data.mapper.TrustMapper
import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportStatus
import io.github.jan.supabase.gotrue.auth
import java.time.Instant

class SupabaseAdminRepository(
    private val remote: AdminRemoteDataSource = AdminRemoteDataSource(),
    private val client: io.github.jan.supabase.SupabaseClient = com.closeby.app.core.network.SupabaseClientProvider.client
) : AdminRepository {

    override suspend fun isAdmin(userId: String): Boolean =
        runCatching {
            remote.getUserProfile(userId)?.isAdmin == true
        }.getOrDefault(false)

    override suspend fun getDashboardStats(): Result<AdminDashboardStats> =
        requireAdmin {
            remote.getDashboardStats().let(AdminMapper::toDomain)
        }

    override suspend fun listAccountDeletionRequests(): Result<List<AdminDeletionRequestSummary>> =
        requireAdmin {
            val profiles = remote.listUserProfiles().associateBy { it.userId }
            remote.listAccountDeletionRequests().map { dto ->
                AdminMapper.toDomain(dto, profiles[dto.userId]?.displayName)
            }
        }

    override suspend fun approveAccountDeletion(requestId: String, note: String?): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateAccountDeletionStatus(
                requestId,
                AccountDeletionStatusUpdateDto(status = "COMPLETED", processedAt = now)
            )
            logAuditInternal(AdminAction.APPROVE_ACCOUNT_DELETION, "ACCOUNT", requestId, note)
        }

    override suspend fun rejectAccountDeletion(requestId: String, note: String?): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateAccountDeletionStatus(
                requestId,
                AccountDeletionStatusUpdateDto(status = "CANCELLED", processedAt = now)
            )
            logAuditInternal(AdminAction.REJECT_ACCOUNT_DELETION, "ACCOUNT", requestId, note)
        }

    override suspend fun listUsers(): Result<List<AdminUserSummary>> =
        requireAdmin {
            remote.listUserProfiles().map(AdminMapper::toDomain)
        }

    override suspend fun suspendUser(userId: String, reason: String?): Result<Unit> =
        requireAdmin {
            remote.updateUserSuspended(userId, isSuspended = true)
            logAuditInternal(AdminAction.SUSPEND_USER, "USER", userId, reason)
        }

    override suspend fun unsuspendUser(userId: String, reason: String?): Result<Unit> =
        requireAdmin {
            remote.updateUserSuspended(userId, isSuspended = false)
            logAuditInternal(AdminAction.UNSUSPEND_USER, "USER", userId, reason)
        }

    override suspend fun listProviders(search: String?): Result<List<AdminProviderSummary>> =
        requireAdmin {
            remote.listProviders()
                .map(AdminMapper::toDomain)
                .let { providers ->
                    if (search.isNullOrBlank()) {
                        providers
                    } else {
                        val query = search.trim().lowercase()
                        providers.filter {
                            it.name.lowercase().contains(query) ||
                                it.id.lowercase().contains(query)
                        }
                    }
                }
        }

    override suspend fun approveVerification(providerId: String, note: String?): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateProviderVerification(
                providerId,
                ProviderVerificationUpdateDto(
                    verificationStatus = "APPROVED",
                    isVerified = true,
                    verificationNote = note,
                    updatedAt = now
                )
            )
            remote.updateLatestVerificationSubmission(providerId, "APPROVED", note)
            logAuditInternal(AdminAction.APPROVE_VERIFICATION, "PROVIDER", providerId, note)
        }

    override suspend fun rejectVerification(providerId: String, reason: String): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateProviderVerification(
                providerId,
                ProviderVerificationUpdateDto(
                    verificationStatus = "REJECTED",
                    isVerified = false,
                    verificationNote = reason,
                    updatedAt = now
                )
            )
            remote.updateLatestVerificationSubmission(providerId, "REJECTED", reason)
            logAuditInternal(AdminAction.REJECT_VERIFICATION, "PROVIDER", providerId, reason)
        }

    override suspend fun suspendProvider(providerId: String, reason: String?): Result<Unit> =
        requireAdmin {
            remote.suspendProvider(providerId, reason)
            logAuditInternal(AdminAction.SUSPEND_PROVIDER, "PROVIDER", providerId, reason)
        }

    override suspend fun listServices(): Result<List<AdminServiceSummary>> =
        requireAdmin {
            remote.listServices().map(AdminMapper::toDomain)
        }

    override suspend fun disableService(serviceId: String, reason: String?): Result<Unit> =
        requireAdmin {
            remote.setServiceActive(serviceId, isActive = false)
            logAuditInternal(AdminAction.DISABLE_SERVICE, "SERVICE", serviceId, reason)
        }

    override suspend fun enableService(serviceId: String, reason: String?): Result<Unit> =
        requireAdmin {
            remote.setServiceActive(serviceId, isActive = true)
            logAuditInternal(AdminAction.ENABLE_SERVICE, "SERVICE", serviceId, reason)
        }

    override suspend fun listReports(): Result<List<Report>> =
        requireAdmin {
            remote.listReports().mapNotNull(TrustMapper::toDomain)
        }

    override suspend fun updateReportStatus(
        reportId: String,
        status: ReportStatus,
        note: String?
    ): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            val resolvedAt = if (status == ReportStatus.RESOLVED || status == ReportStatus.DISMISSED) {
                now
            } else {
                null
            }
            remote.updateReportStatus(
                reportId,
                ReportStatusUpdateDto(
                    status = status.name,
                    moderationNote = note,
                    resolvedAt = resolvedAt,
                    updatedAt = now
                )
            )
            logAuditInternal(AdminAction.UPDATE_REPORT_STATUS, "REPORT", reportId, note)
        }

    override suspend fun listAdvertisements(): Result<List<AdminAdvertisementSummary>> =
        requireAdmin {
            remote.listAdvertisements().map(AdminMapper::toDomain)
        }

    override suspend fun approveAd(adId: String, note: String?): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateAdvertisementStatus(
                adId,
                AdvertisementStatusUpdateDto(
                    status = "APPROVED",
                    rejectionReason = null,
                    approvedAt = now,
                    updatedAt = now
                )
            )
            logAuditInternal(AdminAction.APPROVE_AD, "ADVERTISEMENT", adId, note)
        }

    override suspend fun rejectAd(adId: String, reason: String): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateAdvertisementStatus(
                adId,
                AdvertisementStatusUpdateDto(
                    status = "REJECTED",
                    rejectionReason = reason,
                    approvedAt = null,
                    updatedAt = now
                )
            )
            logAuditInternal(AdminAction.REJECT_AD, "ADVERTISEMENT", adId, reason)
        }

    override suspend fun pauseAd(adId: String, reason: String?): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateAdvertisementStatus(
                adId,
                AdvertisementStatusUpdateDto(
                    status = "PAUSED",
                    rejectionReason = reason,
                    approvedAt = null,
                    updatedAt = now
                )
            )
            logAuditInternal(AdminAction.PAUSE_AD, "ADVERTISEMENT", adId, reason)
        }

    override suspend fun resumeAd(adId: String, reason: String?): Result<Unit> =
        requireAdmin {
            val now = Instant.now().toString()
            remote.updateAdvertisementStatus(
                adId,
                AdvertisementStatusUpdateDto(
                    status = "APPROVED",
                    rejectionReason = null,
                    approvedAt = now,
                    updatedAt = now
                )
            )
            logAuditInternal(AdminAction.RESUME_AD, "ADVERTISEMENT", adId, reason)
        }

    override suspend fun logAudit(
        action: AdminAction,
        targetType: String,
        targetId: String,
        reason: String?
    ): Result<Unit> =
        requireAdmin {
            logAuditInternal(action, targetType, targetId, reason)
        }

    private suspend fun <T> requireAdmin(block: suspend () -> T): Result<T> =
        runCatching {
            val adminId = currentUserId() ?: throw AdminAccessDeniedException()
            if (!isAdmin(adminId)) throw AdminAccessDeniedException()
            block()
        }

    private suspend fun logAuditInternal(
        action: AdminAction,
        targetType: String,
        targetId: String,
        reason: String?
    ) {
        val adminId = currentUserId() ?: throw AdminAccessDeniedException()
        remote.insertAuditLog(
            AdminAuditLogInsertDto(
                adminId = adminId,
                action = action.name,
                targetType = targetType,
                targetId = targetId,
                reason = reason
            )
        )
    }

    private fun currentUserId(): String? =
        client.auth.currentUserOrNull()?.id
}
