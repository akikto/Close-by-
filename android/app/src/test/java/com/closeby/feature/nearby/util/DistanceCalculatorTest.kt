package com.closeby.feature.nearby.util

import com.closeby.feature.nearby.model.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {

    // Deterministic reference coordinates.
    private val sfDowntown = Coordinates(37.7749, -122.4194) // San Francisco
    private val oakland = Coordinates(37.8044, -122.2712)    // ~13.4 km from SF
    private val samePoint = Coordinates(37.7749, -122.4194)

    @Test
    fun `distance between identical points is zero`() {
        val distance = DistanceCalculator.distanceMeters(sfDowntown, samePoint)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `distance is symmetric`() {
        val ab = DistanceCalculator.distanceMeters(sfDowntown, oakland)
        val ba = DistanceCalculator.distanceMeters(oakland, sfDowntown)
        assertEquals(ab, ba, 0.001)
    }

    @Test
    fun `distance between SF and Oakland is approximately correct`() {
        // Known real-world great-circle distance is ~13.4 km.
        val km = DistanceCalculator.distanceKilometers(sfDowntown, oakland)
        assertTrue("expected ~13.4km, got $km", km in 12.5..14.5)
    }

    @Test
    fun `one degree of latitude is approximately 111km`() {
        val a = Coordinates(0.0, 0.0)
        val b = Coordinates(1.0, 0.0)
        val km = DistanceCalculator.distanceKilometers(a, b)
        assertTrue("expected ~111km, got $km", km in 110.5..111.5)
    }

    @Test
    fun `isWithinRadius true for point inside radius`() {
        // Oakland is ~13.4km from SF downtown.
        assertTrue(DistanceCalculator.isWithinRadius(sfDowntown, oakland, radiusKm = 25.0))
    }

    @Test
    fun `isWithinRadius false for point outside radius`() {
        assertFalse(DistanceCalculator.isWithinRadius(sfDowntown, oakland, radiusKm = 5.0))
    }

    @Test
    fun `isWithinRadius boundary case is inclusive`() {
        // Construct a point at (approximately) exactly 10km north of the origin.
        val origin = Coordinates(0.0, 0.0)
        val kmPerDegreeLat = 111.32
        val target = Coordinates(10.0 / kmPerDegreeLat, 0.0)
        val distanceKm = DistanceCalculator.distanceKilometers(origin, target)

        // Use the actual computed distance as the radius to test the inclusive boundary.
        assertTrue(DistanceCalculator.isWithinRadius(origin, target, radiusKm = distanceKm))
    }
}
