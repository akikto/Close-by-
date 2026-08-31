package com.closeby.advertisement.domain.repository

import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.advertisement.domain.model.AdvertisementInput

interface AdvertisementRepository {
    suspend fun createAd(ownerId: String, input: AdvertisementInput): Result<Advertisement>
    suspend fun getMyAds(ownerId: String): Result<List<Advertisement>>
    suspend fun getApprovedAds(): Result<List<Advertisement>>

    suspend fun getPendingAds(): Result<List<Advertisement>>
    suspend fun approveAd(adId: String, adminUserId: String): Result<Advertisement>
    suspend fun rejectAd(adId: String, adminUserId: String, reason: String): Result<Advertisement>
    suspend fun pauseAd(adId: String, ownerId: String): Result<Advertisement>
}
