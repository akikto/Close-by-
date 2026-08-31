package com.closeby.advertisement.data.repository

import com.closeby.advertisement.data.mapper.AdvertisementMapper
import com.closeby.advertisement.data.remote.AdvertisementRemoteDataSource
import com.closeby.advertisement.domain.model.AdStatus
import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.advertisement.domain.model.AdvertisementInput
import com.closeby.advertisement.domain.repository.AdvertisementRepository
import com.closeby.notification.domain.handler.NotificationEventPublisher
import com.closeby.advertisement.domain.validation.AdValidator
import java.time.Instant

class SupabaseAdvertisementRepository(
    private val remote: AdvertisementRemoteDataSource = AdvertisementRemoteDataSource()
) : AdvertisementRepository {

    override suspend fun createAd(ownerId: String, input: AdvertisementInput): Result<Advertisement> =
        runCatching {
            AdValidator.validate(input).getOrThrow()
            val dto = remote.insert(AdvertisementMapper.toInsertDto(ownerId, input))
            val ad = AdvertisementMapper.toDomain(dto)
                ?: throw IllegalStateException("Created advertisement has invalid data.")
            NotificationEventPublisher.adSubmitted(ownerId, ad.id)
            ad
        }

    override suspend fun getMyAds(ownerId: String): Result<List<Advertisement>> = runCatching {
        remote.getByOwner(ownerId).mapNotNull(AdvertisementMapper::toDomain)
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getApprovedAds(): Result<List<Advertisement>> = runCatching {
        remote.getApproved().mapNotNull(AdvertisementMapper::toDomain)
    }

    override suspend fun getPendingAds(): Result<List<Advertisement>> = runCatching {
        remote.getPending().mapNotNull(AdvertisementMapper::toDomain)
            .sortedBy { it.createdAt }
    }

    override suspend fun approveAd(adId: String, adminUserId: String): Result<Advertisement> =
        runCatching {
            val dto = remote.updateStatus(
                id = adId,
                status = AdStatus.APPROVED,
                approvedBy = adminUserId,
                approvedAt = Instant.now().toString()
            )
            AdvertisementMapper.toDomain(dto)
                ?: throw IllegalStateException("Updated advertisement has invalid data.")
        }

    override suspend fun rejectAd(
        adId: String,
        adminUserId: String,
        reason: String
    ): Result<Advertisement> = runCatching {
        val dto = remote.updateStatus(
            id = adId,
            status = AdStatus.REJECTED,
            approvedBy = adminUserId,
            approvedAt = Instant.now().toString(),
            rejectionReason = reason.trim().ifBlank { "Rejected by admin." }
        )
        AdvertisementMapper.toDomain(dto)
            ?: throw IllegalStateException("Updated advertisement has invalid data.")
    }

    override suspend fun pauseAd(adId: String, ownerId: String): Result<Advertisement> = runCatching {
        val existing = remote.getById(adId) ?: throw NoSuchElementException("Advertisement not found.")
        if (existing.ownerId != ownerId) {
            throw SecurityException("You can only pause your own advertisements.")
        }
        val dto = remote.updateStatus(id = adId, status = AdStatus.PAUSED)
        AdvertisementMapper.toDomain(dto)
            ?: throw IllegalStateException("Updated advertisement has invalid data.")
    }
}
