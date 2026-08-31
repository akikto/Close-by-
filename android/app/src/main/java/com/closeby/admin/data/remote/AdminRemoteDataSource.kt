package com.closeby.admin.data.remote

import com.closeby.admin.data.model.AdminAuditLogInsertDto
import com.closeby.admin.data.model.AdminDashboardStatsDto
import com.closeby.admin.data.model.AdvertisementAdminDto
import com.closeby.admin.data.model.AdvertisementStatusUpdateDto
import com.closeby.admin.data.model.ProviderAdminDto
import com.closeby.admin.data.model.ProviderVerificationUpdateDto
import com.closeby.admin.data.model.ReportStatusUpdateDto
import com.closeby.admin.data.model.ServiceActiveAdminUpdateDto
import com.closeby.admin.data.model.ServiceAdminDto
import com.closeby.admin.data.model.UserProfileDto
import com.closeby.admin.data.model.UserProfileSuspendUpdateDto
import com.closeby.admin.data.model.VerificationSubmissionStatusUpdateDto
import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.trust.data.model.ReportDto
import com.closeby.trust.data.model.VerificationSubmissionDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant

class AdminRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    private val serviceColumns = """
        id,
        provider_id,
        title,
        category,
        is_active,
        deleted_at,
        providers ( name )
    """.trimIndent()

    suspend fun getUserProfile(userId: String): UserProfileDto? =
        client.from("user_profiles")
            .select { filter { eq("user_id", userId) } }
            .decodeList<UserProfileDto>()
            .firstOrNull()

    suspend fun getDashboardStats(): AdminDashboardStatsDto =
        client.from("admin_dashboard_stats")
            .select()
            .decodeSingle<AdminDashboardStatsDto>()

    suspend fun listUserProfiles(): List<UserProfileDto> =
        client.from("user_profiles")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<UserProfileDto>()

    suspend fun updateUserSuspended(userId: String, isSuspended: Boolean) {
        client.from("user_profiles")
            .update(
                UserProfileSuspendUpdateDto(
                    isSuspended = isSuspended,
                    updatedAt = Instant.now().toString()
                )
            ) {
                filter { eq("user_id", userId) }
            }
    }

    suspend fun listProviders(): List<ProviderAdminDto> =
        client.from("providers")
            .select {
                order("name", Order.ASCENDING)
            }
            .decodeList<ProviderAdminDto>()

    suspend fun updateProviderVerification(
        providerId: String,
        dto: ProviderVerificationUpdateDto
    ) {
        client.from("providers")
            .update(dto) {
                filter { eq("id", providerId) }
            }
    }

    suspend fun updateLatestVerificationSubmission(
        providerId: String,
        status: String,
        adminNote: String?
    ) {
        val submission = client.from("provider_verification_submissions")
            .select {
                filter { eq("provider_id", providerId) }
                order("created_at", Order.DESCENDING)
                limit(1)
            }
            .decodeList<VerificationSubmissionDto>()
            .firstOrNull() ?: return

        val now = Instant.now().toString()
        client.from("provider_verification_submissions")
            .update(
                VerificationSubmissionStatusUpdateDto(
                    status = status,
                    adminNote = adminNote,
                    reviewedAt = now,
                    updatedAt = now
                )
            ) {
                filter { eq("id", submission.id) }
            }
    }

    suspend fun suspendProvider(providerId: String, reason: String?) {
        val now = Instant.now().toString()
        client.from("providers")
            .update(
                ProviderVerificationUpdateDto(
                    verificationStatus = "SUSPENDED",
                    isVerified = false,
                    verificationNote = reason,
                    isSuspended = true,
                    updatedAt = now
                )
            ) {
                filter { eq("id", providerId) }
            }
    }

    suspend fun listServices(): List<ServiceAdminDto> =
        client.from("services")
            .select(columns = Columns.raw(serviceColumns)) {
                order("title", Order.ASCENDING)
            }
            .decodeList<ServiceAdminDto>()

    suspend fun setServiceActive(serviceId: String, isActive: Boolean) {
        client.from("services")
            .update(
                ServiceActiveAdminUpdateDto(
                    isActive = isActive,
                    updatedAt = Instant.now().toString()
                )
            ) {
                filter { eq("id", serviceId) }
            }
    }

    suspend fun listReports(): List<ReportDto> =
        client.from("reports")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ReportDto>()

    suspend fun updateReportStatus(reportId: String, dto: ReportStatusUpdateDto) {
        client.from("reports")
            .update(dto) {
                filter { eq("id", reportId) }
            }
    }

    suspend fun listAdvertisements(): List<AdvertisementAdminDto> =
        client.from("advertisements")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<AdvertisementAdminDto>()

    suspend fun updateAdvertisementStatus(adId: String, dto: AdvertisementStatusUpdateDto) {
        client.from("advertisements")
            .update(dto) {
                filter { eq("id", adId) }
            }
    }

    suspend fun insertAuditLog(dto: AdminAuditLogInsertDto) {
        client.from("admin_audit_logs")
            .insert(dto)
    }
}
