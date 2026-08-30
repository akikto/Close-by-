package com.closeby.feature.nearby.util

import com.closeby.feature.nearby.model.Coordinates
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure geographic distance math. No Android/framework dependency, no third-party
 * library — just the Haversine formula, so this is trivially unit testable on the JVM.
 */
object DistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Great-circle distance between two points, in meters, using the Haversine formula.
     * Accurate to within ~0.5% for typical nearby-search distances (city / regional scale).
     */
    fun distanceMeters(from: Coordinates, to: Coordinates): Double {
        val lat1Rad = Math.toRadians(from.latitude)
        val lat2Rad = Math.toRadians(to.latitude)
        val deltaLat = Math.toRadians(to.latitude - from.latitude)
        val deltaLon = Math.toRadians(to.longitude - from.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    fun distanceKilometers(from: Coordinates, to: Coordinates): Double =
        distanceMeters(from, to) / 1000.0

    /** True if [to] lies within [radiusKm] kilometers of [from] (inclusive boundary). */
    fun isWithinRadius(from: Coordinates, to: Coordinates, radiusKm: Double): Boolean =
        distanceKilometers(from, to) <= radiusKm
}
