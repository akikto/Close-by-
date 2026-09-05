package com.closeby.admin.data.repository

import com.closeby.admin.domain.model.AdminAccessDeniedException
import com.closeby.admin.domain.model.AdminAction
import com.closeby.admin.domain.model.AdminAdvertisementSummary
import com.closeby.admin.domain.model.AdminAuditLog
import com.closeby.admin.domain.model.AdminDashboardStats
import com.closeby.admin.domain.model.AdminDeletionRequestSummary
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.admin.domain.model.AdminServiceSummary
import com.closeby.admin.domain.model.AdminUserSummary
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportReason
import com.closeby.trust.domain.model.ReportStatus
import com.closeby.trust.domain.model.ReportTargetType
import com.closeby.trust.domain.model.VerificationStatus
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory admin repository for local development without Supabase.
 * Admin status is stored in [userProfiles] and must be set server-side in production.
 */
class MockAdminRepository(
    private val currentUserIdProvider: () -> String?
) : AdminRepository {

    private val userProfiles = ConcurrentHashMap<String, AdminUserSummary>()
    private val providers = ConcurrentHashMap<String, AdminProviderSummary>()
    private val services = ConcurrentHashMap<String, AdminServiceSummary>()
    private val reports = ConcurrentHashMap<String, Report>()
    private val advertisements = ConcurrentHashMap<String, AdminAdvertisementSummary>()
    private val auditLogs = mutableListOf<AdminAuditLog>()
    private val deletionRequests = ConcurrentHashMap<String, AdminDeletionRequestSummary>()

    init {
        seedDemoData()
    }

    override suspend fun isAdmin(userId: String): Boolean =
        userProfiles[userId]?.isAdmin == true

    override suspend fun getDashboardStats(): Result<AdminDashboardStats> =
        requireAdmin {
            AdminDashboardStats(
                totalUsers = userProfiles.size,
                totalProviders = providers.size,
                activeServices = services.count { it.value.isActive && !it.value.isDeleted },
                pendingVerifications = providers.count {
                    it.value.verificationStatus == VerificationStatus.PENDING
                },
                pendingAdvertisements = advertisements.count { it.value.status == "PENDING" },
                openReports = reports.count { it.value.status == ReportStatus.OPEN },
                pendingDeletionRequests = deletionRequests.count { it.value.status == "PENDING" }
            )
        }

    override suspend fun listAccountDeletionRequests(): Result<List<AdminDeletionRequestSummary>> =
        requireAdmin {
            deletionRequests.values.sortedByDescending { it.requestedAt }
        }

    override suspend fun approveAccountDeletion(requestId: String, note: String?): Result<Unit> =
        requireAdmin {
            val existing = deletionRequests[requestId] ?: throw NoSuchElementException("Request not found.")
            deletionRequests[requestId] = existing.copy(status = "COMPLETED", processedAt = System.currentTimeMillis())
            recordAudit(AdminAction.APPROVE_ACCOUNT_DELETION, "ACCOUNT", requestId, note)
        }

    override suspend fun rejectAccountDeletion(requestId: String, note: String?): Result<Unit> =
        requireAdmin {
            val existing = deletionRequests[requestId] ?: throw NoSuchElementException("Request not found.")
            deletionRequests[requestId] = existing.copy(status = "CANCELLED", processedAt = System.currentTimeMillis())
            recordAudit(AdminAction.REJECT_ACCOUNT_DELETION, "ACCOUNT", requestId, note)
        }

    override suspend fun listUsers(): Result<List<AdminUserSummary>> =
        requireAdmin { userProfiles.values.sortedByDescending { it.createdAt } }

    override suspend fun suspendUser(userId: String, reason: String?): Result<Unit> =
        requireAdmin {
            val profile = userProfiles[userId] ?: throw NoSuchElementException("User not found.")
            userProfiles[userId] = profile.copy(isSuspended = true)
            recordAudit(AdminAction.SUSPEND_USER, "USER", userId, reason)
        }

    override suspend fun unsuspendUser(userId: String, reason: String?): Result<Unit> =
        requireAdmin {
            val profile = userProfiles[userId] ?: throw NoSuchElementException("User not found.")
            userProfiles[userId] = profile.copy(isSuspended = false)
            recordAudit(AdminAction.UNSUSPEND_USER, "USER", userId, reason)
        }

    override suspend fun listProviders(search: String?): Result<List<AdminProviderSummary>> =
        requireAdmin {
            providers.values
                .filter { provider ->
                    if (search.isNullOrBlank()) true
                    else {
                        val query = search.trim().lowercase()
                        provider.name.lowercase().contains(query) ||
                            provider.id.lowercase().contains(query)
                    }
                }
                .sortedBy { it.name }
        }

    override suspend fun approveVerification(providerId: String, note: String?): Result<Unit> =
        requireAdmin {
            updateProviderVerification(providerId, VerificationStatus.APPROVED)
            recordAudit(AdminAction.APPROVE_VERIFICATION, "PROVIDER", providerId, note)
        }

    override suspend fun rejectVerification(providerId: String, reason: String): Result<Unit> =
        requireAdmin {
            updateProviderVerification(providerId, VerificationStatus.REJECTED)
            recordAudit(AdminAction.REJECT_VERIFICATION, "PROVIDER", providerId, reason)
        }

    override suspend fun suspendProvider(providerId: String, reason: String?): Result<Unit> =
        requireAdmin {
            val provider = providers[providerId] ?: throw NoSuchElementException("Provider not found.")
            providers[providerId] = provider.copy(
                verificationStatus = VerificationStatus.SUSPENDED,
                isSuspended = true
            )
            recordAudit(AdminAction.SUSPEND_PROVIDER, "PROVIDER", providerId, reason)
        }

    override suspend fun listServices(): Result<List<AdminServiceSummary>> =
        requireAdmin { services.values.sortedBy { it.title } }

    override suspend fun disableService(serviceId: String, reason: String?): Result<Unit> =
        requireAdmin {
            val service = services[serviceId] ?: throw NoSuchElementException("Service not found.")
            services[serviceId] = service.copy(isActive = false)
            recordAudit(AdminAction.DISABLE_SERVICE, "SERVICE", serviceId, reason)
        }

    override suspend fun enableService(serviceId: String, reason: String?): Result<Unit> =
        requireAdmin {
            val service = services[serviceId] ?: throw NoSuchElementException("Service not found.")
            services[serviceId] = service.copy(isActive = true)
            recordAudit(AdminAction.ENABLE_SERVICE, "SERVICE", serviceId, reason)
        }

    override suspend fun listReports(): Result<List<Report>> =
        requireAdmin { reports.values.sortedByDescending { it.createdAt } }

    override suspend fun updateReportStatus(
        reportId: String,
        status: ReportStatus,
        note: String?
    ): Result<Unit> =
        requireAdmin {
            val report = reports[reportId] ?: throw NoSuchElementException("Report not found.")
            reports[reportId] = report.copy(
                status = status,
                resolvedAt = if (status == ReportStatus.RESOLVED || status == ReportStatus.DISMISSED) {
                    System.currentTimeMillis()
                } else {
                    report.resolvedAt
                }
            )
            recordAudit(AdminAction.UPDATE_REPORT_STATUS, "REPORT", reportId, note)
        }

    override suspend fun listAdvertisements(): Result<List<AdminAdvertisementSummary>> =
        requireAdmin { advertisements.values.sortedByDescending { it.createdAt } }

    override suspend fun approveAd(adId: String, note: String?): Result<Unit> =
        requireAdmin {
            updateAdStatus(adId, "APPROVED")
            recordAudit(AdminAction.APPROVE_AD, "ADVERTISEMENT", adId, note)
        }

    override suspend fun rejectAd(adId: String, reason: String): Result<Unit> =
        requireAdmin {
            updateAdStatus(adId, "REJECTED")
            recordAudit(AdminAction.REJECT_AD, "ADVERTISEMENT", adId, reason)
        }

    override suspend fun pauseAd(adId: String, reason: String?): Result<Unit> =
        requireAdmin {
            updateAdStatus(adId, "PAUSED")
            recordAudit(AdminAction.PAUSE_AD, "ADVERTISEMENT", adId, reason)
        }

    override suspend fun resumeAd(adId: String, reason: String?): Result<Unit> =
        requireAdmin {
            updateAdStatus(adId, "APPROVED")
            recordAudit(AdminAction.RESUME_AD, "ADVERTISEMENT", adId, reason)
        }

    override suspend fun logAudit(
        action: AdminAction,
        targetType: String,
        targetId: String,
        reason: String?
    ): Result<Unit> =
        requireAdmin {
            recordAudit(action, targetType, targetId, reason)
        }

    fun setUserAdmin(userId: String, isAdmin: Boolean) {
        val existing = userProfiles[userId]
        if (existing != null) {
            userProfiles[userId] = existing.copy(isAdmin = isAdmin)
        } else {
            userProfiles[userId] = AdminUserSummary(
                userId = userId,
                displayName = null,
                isSuspended = false,
                isAdmin = isAdmin,
                createdAt = System.currentTimeMillis()
            )
        }
    }

    fun auditLogs(): List<AdminAuditLog> = auditLogs.toList()

    private suspend fun <T> requireAdmin(block: suspend () -> T): Result<T> =
        runCatching {
            val userId = currentUserIdProvider()
                ?: throw AdminAccessDeniedException()
            if (!isAdmin(userId)) throw AdminAccessDeniedException()
            block()
        }

    private fun recordAudit(
        action: AdminAction,
        targetType: String,
        targetId: String,
        reason: String?
    ) {
        val adminId = currentUserIdProvider() ?: throw AdminAccessDeniedException()
        auditLogs += AdminAuditLog(
            id = UUID.randomUUID().toString(),
            adminId = adminId,
            action = action,
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun updateProviderVerification(providerId: String, status: VerificationStatus) {
        val provider = providers[providerId] ?: throw NoSuchElementException("Provider not found.")
        providers[providerId] = provider.copy(verificationStatus = status)
    }

    private fun updateAdStatus(adId: String, status: String) {
        val ad = advertisements[adId] ?: throw NoSuchElementException("Advertisement not found.")
        advertisements[adId] = ad.copy(status = status)
    }

    private fun seedDemoData() {
        val now = System.currentTimeMillis()
        userProfiles["demo-user"] = AdminUserSummary(
            userId = "demo-user",
            displayName = "Demo User",
            isSuspended = false,
            isAdmin = false,
            createdAt = now
        )
        providers["11111111-1111-1111-1111-111111111101"] = AdminProviderSummary(
            id = "11111111-1111-1111-1111-111111111101",
            name = "Ravi Kumar",
            userId = "demo-user",
            verificationStatus = VerificationStatus.PENDING,
            isSuspended = false,
            isActive = true,
            rating = 4.7,
            reviewCount = 32
        )
        providers["11111111-1111-1111-1111-111111111102"] = AdminProviderSummary(
            id = "11111111-1111-1111-1111-111111111102",
            name = "Suresh Electricals",
            userId = null,
            verificationStatus = VerificationStatus.APPROVED,
            isSuspended = false,
            isActive = true,
            rating = 4.2,
            reviewCount = 10
        )
        services["22222222-2222-2222-2222-222222222201"] = AdminServiceSummary(
            id = "22222222-2222-2222-2222-222222222201",
            providerId = "11111111-1111-1111-1111-111111111101",
            providerName = "Ravi Kumar",
            title = "Water Pump",
            category = "EQUIPMENT",
            isActive = true,
            isDeleted = false
        )
        reports["report-001"] = Report(
            id = "report-001",
            reporterId = "demo-user",
            targetType = ReportTargetType.SERVICE,
            targetId = "22222222-2222-2222-2222-222222222201",
            reason = ReportReason.FAKE_LISTING,
            description = "Listing appears fraudulent.",
            status = ReportStatus.OPEN,
            createdAt = now,
            resolvedAt = null
        )
        advertisements["ad-001"] = AdminAdvertisementSummary(
            id = "ad-001",
            ownerId = "demo-user",
            businessName = "Local Repairs",
            title = "20% off plumbing",
            status = "PENDING",
            startAt = now,
            endAt = now + 86_400_000L,
            createdAt = now
        )
        deletionRequests["del-req-001"] = AdminDeletionRequestSummary(
            id = "del-req-001",
            userId = "demo-user",
            displayName = "Demo User",
            reason = "No longer using the app",
            status = "PENDING",
            requestedAt = now,
            processedAt = null
        )
    }
}
