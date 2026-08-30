package com.closeby.feature.nearby.util

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Formats a distance in meters into a short, human-readable label such as
 * "850 m away" or "2.4 km away".
 *
 * Rule of thumb: below 1 km, show whole meters; at/above 1 km, show kilometers
 * to one decimal place.
 */
object DistanceFormatter {

    private const val METERS_PER_KM = 1000.0

    /** e.g. "850 m", "2.4 km" — no trailing "away" suffix. */
    fun format(distanceMeters: Double): String {
        require(distanceMeters >= 0.0) { "distanceMeters must be >= 0, was $distanceMeters" }
        return if (distanceMeters < METERS_PER_KM) {
            "${distanceMeters.roundToInt()} m"
        } else {
            val km = distanceMeters / METERS_PER_KM
            String.format(Locale.US, "%.1f km", km)
        }
    }

    /** e.g. "850 m away", "2.4 km away" — for direct display in nearby-listing UI. */
    fun formatWithSuffix(distanceMeters: Double): String = "${format(distanceMeters)} away"
}
