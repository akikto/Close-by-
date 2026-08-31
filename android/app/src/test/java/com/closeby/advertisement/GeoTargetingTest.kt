package com.closeby.advertisement

import com.closeby.advertisement.domain.GeoTargeting
import com.closeby.advertisement.domain.model.AdStatus
import com.closeby.advertisement.domain.model.Advertisement
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTargetingTest {

    private val baseAd = Advertisement(
        id = "ad-test",
        ownerId = "owner-1",
        businessName = "Test Shop",
        title = "Test Offer",
        description = "A test advertisement.",
        imageUrl = "https://example.com/ad.jpg",
        contactNumber = "+8801712345678",
        latitude = 12.9716,
        longitude = 77.5946,
        targetRadiusMeters = 5_000,
        startAt = 1_000L,
        endAt = 10_000L,
        status = AdStatus.APPROVED,
        approvedBy = "admin-1",
        approvedAt = 500L,
        rejectionReason = null,
        createdAt = 0L,
        updatedAt = 0L
    )

    @Test
    fun `user within radius is eligible`() {
        val eligible = GeoTargeting.isEligible(
            ad = baseAd,
            userLat = 12.9720,
            userLng = 77.5950,
            now = 5_000L
        )
        assertTrue(eligible)
    }

    @Test
    fun `user outside radius is not eligible`() {
        val eligible = GeoTargeting.isEligible(
            ad = baseAd,
            userLat = 13.1000,
            userLng = 77.8000,
            now = 5_000L
        )
        assertFalse(eligible)
    }

    @Test
    fun `ad before start date is not eligible`() {
        val eligible = GeoTargeting.isEligible(
            ad = baseAd,
            userLat = 12.9716,
            userLng = 77.5946,
            now = 500L
        )
        assertFalse(eligible)
    }

    @Test
    fun `expired ad is not eligible`() {
        val eligible = GeoTargeting.isEligible(
            ad = baseAd,
            userLat = 12.9716,
            userLng = 77.5946,
            now = 11_000L
        )
        assertFalse(eligible)
    }

    @Test
    fun `pending ad is hidden from public feed`() {
        val pending = baseAd.copy(status = AdStatus.PENDING)
        val eligible = GeoTargeting.isEligible(
            ad = pending,
            userLat = 12.9716,
            userLng = 77.5946,
            now = 5_000L
        )
        assertFalse(eligible)
    }
}
