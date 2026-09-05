package com.closeby.admin.data.mapper

import com.closeby.admin.data.model.AccountDeletionRequestDto
import com.closeby.admin.data.model.AdminDashboardStatsDto
import com.closeby.admin.data.model.AdvertisementAdminDto
import com.closeby.admin.data.model.ProviderAdminDto
import com.closeby.admin.data.model.ServiceAdminDto
import com.closeby.admin.data.model.UserProfileDto
import com.closeby.admin.domain.model.AdminAdvertisementSummary
import com.closeby.admin.domain.model.AdminDashboardStats
import com.closeby.admin.domain.model.AdminDeletionRequestSummary
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.admin.domain.model.AdminServiceSummary
import com.closeby.admin.domain.model.AdminUserSummary
import com.closeby.trust.domain.model.VerificationStatus
import java.time.Instant

object AdminMapper {

    fun toDomain(dto: AdminDashboardStatsDto): AdminDashboardStats =
        AdminDashboardStats(
            totalUsers = dto.totalUsers,
            totalProviders = dto.totalProviders,
            activeServices = dto.activeServices,
            pendingVerifications = dto.pendingVerifications,
            pendingAdvertisements = dto.pendingAdvertisements,
            openReports = dto.openReports,
            pendingDeletionRequests = dto.pendingDeletionRequests
        )

    fun toDomain(dto: AccountDeletionRequestDto, displayName: String? = null): AdminDeletionRequestSummary =
        AdminDeletionRequestSummary(
            id = dto.id,
            userId = dto.userId,
            displayName = displayName,
            reason = dto.reason,
            status = dto.status,
            requestedAt = parseInstant(dto.requestedAt),
            processedAt = dto.processedAt?.let(::parseInstant)
        )

    fun toDomain(dto: UserProfileDto): AdminUserSummary =
        AdminUserSummary(
            userId = dto.userId,
            displayName = dto.displayName,
            isSuspended = dto.isSuspended,
            isAdmin = dto.isAdmin,
            createdAt = dto.createdAt?.let(::parseInstant) ?: 0L
        )

    fun toDomain(dto: ProviderAdminDto): AdminProviderSummary =
        AdminProviderSummary(
            id = dto.id,
            name = dto.name,
            userId = dto.userId,
            verificationStatus = VerificationStatus.fromRaw(dto.verificationStatus),
            isSuspended = dto.isSuspended,
            isActive = dto.isActive,
            rating = dto.rating,
            reviewCount = dto.reviewCount
        )

    fun toDomain(dto: ServiceAdminDto): AdminServiceSummary =
        AdminServiceSummary(
            id = dto.id,
            providerId = dto.providerId,
            providerName = dto.providers?.name,
            title = dto.title,
            category = dto.category,
            isActive = dto.isActive,
            isDeleted = dto.deletedAt != null
        )

    fun toDomain(dto: AdvertisementAdminDto): AdminAdvertisementSummary =
        AdminAdvertisementSummary(
            id = dto.id,
            ownerId = dto.ownerId,
            businessName = dto.businessName,
            title = dto.title,
            status = dto.status,
            startAt = parseInstant(dto.startAt),
            endAt = parseInstant(dto.endAt),
            createdAt = parseInstant(dto.createdAt)
        )

    private fun parseInstant(raw: String): Long =
        Instant.parse(raw).toEpochMilli()
}
