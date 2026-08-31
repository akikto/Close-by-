package com.closeby.advertisement.domain

import com.closeby.advertisement.domain.model.AdStatus
import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.feature.nearby.model.Coordinates
import com.closeby.feature.nearby.util.DistanceCalculator
import com.closeby.feature.nearby.util.DistanceFormatter

object GeoTargeting {

    fun isEligible(
        ad: Advertisement,
        userLat: Double,
        userLng: Double,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        if (ad.status != AdStatus.APPROVED) return false
        if (now < ad.startAt || now > ad.endAt) return false
        val distanceMeters = distanceMetersToAd(ad, userLat, userLng)
        return distanceMeters <= ad.targetRadiusMeters
    }

    fun formatDistanceLabel(ad: Advertisement, userLat: Double, userLng: Double): String {
        val distanceMeters = distanceMetersToAd(ad, userLat, userLng)
        return DistanceFormatter.formatWithSuffix(distanceMeters)
    }

    private fun distanceMetersToAd(ad: Advertisement, userLat: Double, userLng: Double): Double =
        DistanceCalculator.distanceMeters(
            from = Coordinates(userLat, userLng),
            to = Coordinates(ad.latitude, ad.longitude)
        )
}
