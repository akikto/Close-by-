package com.closeby.feature.nearby.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatterTest {

    @Test
    fun `formats sub-kilometer distances as whole meters`() {
        assertEquals("850 m", DistanceFormatter.format(850.0))
    }

    @Test
    fun `rounds meters to nearest whole number`() {
        assertEquals("851 m", DistanceFormatter.format(850.6))
        assertEquals("850 m", DistanceFormatter.format(850.4))
    }

    @Test
    fun `formats kilometer distances with one decimal`() {
        assertEquals("2.4 km", DistanceFormatter.format(2400.0))
        assertEquals("12.8 km", DistanceFormatter.format(12_800.0))
    }

    @Test
    fun `boundary at exactly 1000 meters switches to km format`() {
        assertEquals("1.0 km", DistanceFormatter.format(1000.0))
    }

    @Test
    fun `boundary just under 1000 meters stays in meters`() {
        assertEquals("999 m", DistanceFormatter.format(999.0))
    }

    @Test
    fun `zero distance formats correctly`() {
        assertEquals("0 m", DistanceFormatter.format(0.0))
    }

    @Test
    fun `formatWithSuffix appends away`() {
        assertEquals("850 m away", DistanceFormatter.formatWithSuffix(850.0))
        assertEquals("2.4 km away", DistanceFormatter.formatWithSuffix(2400.0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative distance throws`() {
        DistanceFormatter.format(-1.0)
    }
}
