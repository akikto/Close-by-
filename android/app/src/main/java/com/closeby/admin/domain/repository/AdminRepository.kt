package com.closeby.admin.domain.repository

import com.closeby.admin.domain.model.AdminAction
import com.closeby.admin.domain.model.AdminAdvertisementSummary
import com.closeby.admin.domain.model.AdminDashboardStats
import com.closeby.admin.domain.model.AdminDeletionRequestSummary
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.admin.domain.model.AdminServiceSummary
import com.closeby.admin.domain.model.AdminUserSummary
import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportStatus

interface AdminRepository {

    suspend fun isAdmin(userId: String): Boolean

    suspend fun getDashboardStats(): Result<AdminDashboardStats>

    suspend fun listAccountDeletionRequests(): Result<List<AdminDeletionRequestSummary>>

    suspend fun approveAccountDeletion(requestId: String, note: String? = null): Result<Unit>

    suspend fun rejectAccountDeletion(requestId: String, note: String? = null): Result<Unit>

    suspend fun listUsers(): Result<List<AdminUserSummary>>

    suspend fun suspendUser(userId: String, reason: String?): Result<Unit>

    suspend fun unsuspendUser(userId: String, reason: String?): Result<Unit>

    suspend fun listProviders(search: String? = null): Result<List<AdminProviderSummary>>

    suspend fun approveVerification(providerId: String, note: String?): Result<Unit>

    suspend fun rejectVerification(providerId: String, reason: String): Result<Unit>

    suspend fun suspendProvider(providerId: String, reason: String?): Result<Unit>

    suspend fun listServices(): Result<List<AdminServiceSummary>>

    suspend fun disableService(serviceId: String, reason: String?): Result<Unit>

    suspend fun enableService(serviceId: String, reason: String?): Result<Unit>

    suspend fun listReports(): Result<List<Report>>

    suspend fun updateReportStatus(
        reportId: String,
        status: ReportStatus,
        note: String?
    ): Result<Unit>

    suspend fun listAdvertisements(): Result<List<AdminAdvertisementSummary>>

    suspend fun approveAd(adId: String, note: String?): Result<Unit>

    suspend fun rejectAd(adId: String, reason: String): Result<Unit>

    suspend fun pauseAd(adId: String, reason: String?): Result<Unit>

    suspend fun resumeAd(adId: String, reason: String?): Result<Unit>

    suspend fun logAudit(
        action: AdminAction,
        targetType: String,
        targetId: String,
        reason: String?
    ): Result<Unit>
}
