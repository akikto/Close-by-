package com.closeby.advertisement.data.mock

import com.closeby.advertisement.domain.model.AdStatus
import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.advertisement.domain.model.AdvertisementInput
import com.closeby.advertisement.domain.repository.AdvertisementRepository
import com.closeby.advertisement.domain.validation.AdValidator
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryAdvertisementRepository : AdvertisementRepository {

    private val ads = CopyOnWriteArrayList(sampleAds())

    override suspend fun createAd(ownerId: String, input: AdvertisementInput): Result<Advertisement> =
        runCatching {
            AdValidator.validate(input).getOrThrow()
            val now = System.currentTimeMillis()
            val ad = Advertisement(
                id = UUID.randomUUID().toString(),
                ownerId = ownerId,
                businessName = input.businessName.trim(),
                title = input.title.trim(),
                description = input.description.trim(),
                imageUrl = input.imageUrl,
                contactNumber = input.contactNumber.trim(),
                latitude = input.latitude,
                longitude = input.longitude,
                targetRadiusMeters = input.targetRadiusMeters,
                startAt = input.startAt,
                endAt = input.endAt,
                status = AdStatus.PENDING,
                approvedBy = null,
                approvedAt = null,
                rejectionReason = null,
                createdAt = now,
                updatedAt = now
            )
            ads.add(ad)
            ad
        }

    override suspend fun getMyAds(ownerId: String): Result<List<Advertisement>> =
        Result.success(ads.filter { it.ownerId == ownerId }.sortedByDescending { it.createdAt })

    override suspend fun getApprovedAds(): Result<List<Advertisement>> =
        Result.success(ads.filter { it.status == AdStatus.APPROVED })

    override suspend fun getPendingAds(): Result<List<Advertisement>> =
        Result.success(ads.filter { it.status == AdStatus.PENDING }.sortedBy { it.createdAt })

    override suspend fun approveAd(adId: String, adminUserId: String): Result<Advertisement> =
        updateStatus(adId, AdStatus.APPROVED, adminUserId)

    override suspend fun rejectAd(
        adId: String,
        adminUserId: String,
        reason: String
    ): Result<Advertisement> = updateStatus(
        adId = adId,
        status = AdStatus.REJECTED,
        adminUserId = adminUserId,
        rejectionReason = reason.trim().ifBlank { "Rejected by admin." }
    )

    override suspend fun pauseAd(adId: String, ownerId: String): Result<Advertisement> = runCatching {
        val index = ads.indexOfFirst { it.id == adId }
        if (index == -1) throw NoSuchElementException("Advertisement not found.")
        val current = ads[index]
        if (current.ownerId != ownerId) throw SecurityException("Not your advertisement.")
        val updated = current.copy(status = AdStatus.PAUSED, updatedAt = System.currentTimeMillis())
        ads[index] = updated
        updated
    }

    private fun updateStatus(
        adId: String,
        status: AdStatus,
        adminUserId: String,
        rejectionReason: String? = null
    ): Result<Advertisement> = runCatching {
        val index = ads.indexOfFirst { it.id == adId }
        if (index == -1) throw NoSuchElementException("Advertisement not found.")
        val now = System.currentTimeMillis()
        val updated = ads[index].copy(
            status = status,
            approvedBy = adminUserId,
            approvedAt = now,
            rejectionReason = rejectionReason,
            updatedAt = now
        )
        ads[index] = updated
        updated
    }

    private fun sampleAds(): List<Advertisement> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return listOf(
            Advertisement(
                id = "ad-1",
                ownerId = "owner-1",
                businessName = "Green Valley Cafe",
                title = "20% off weekend brunch",
                description = "Show this ad at checkout for a discount on brunch plates.",
                imageUrl = "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=800",
                contactNumber = "+8801712345678",
                latitude = 12.9716,
                longitude = 77.5946,
                targetRadiusMeters = 10_000,
                startAt = now - day,
                endAt = now + 7 * day,
                status = AdStatus.APPROVED,
                approvedBy = "admin-1",
                approvedAt = now - day,
                rejectionReason = null,
                createdAt = now - 2 * day,
                updatedAt = now - day
            ),
            Advertisement(
                id = "ad-2",
                ownerId = "owner-2",
                businessName = "TechFix Hub",
                title = "Free diagnostics this week",
                description = "Bring any laptop or phone for a free health check.",
                imageUrl = "https://images.unsplash.com/photo-1581092160562-40aa08e78837?w=800",
                contactNumber = "+8801812345678",
                latitude = 12.9800,
                longitude = 77.6000,
                targetRadiusMeters = 5_000,
                startAt = now - day,
                endAt = now + 3 * day,
                status = AdStatus.APPROVED,
                approvedBy = "admin-1",
                approvedAt = now - day,
                rejectionReason = null,
                createdAt = now - day,
                updatedAt = now - day
            ),
            Advertisement(
                id = "ad-3",
                ownerId = "owner-1",
                businessName = "Pending Shop",
                title = "Awaiting approval",
                description = "This ad should not appear publicly.",
                imageUrl = "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=800",
                contactNumber = "+8801712345678",
                latitude = 12.9716,
                longitude = 77.5946,
                targetRadiusMeters = 5_000,
                startAt = now,
                endAt = now + day,
                status = AdStatus.PENDING,
                approvedBy = null,
                approvedAt = null,
                rejectionReason = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
