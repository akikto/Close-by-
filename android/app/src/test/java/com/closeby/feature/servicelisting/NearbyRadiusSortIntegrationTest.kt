package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockLocationProvider
import com.closeby.feature.servicelisting.domain.model.DistanceInfo
import com.closeby.feature.servicelisting.domain.model.LocationStatus
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.model.RadiusOption
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import com.closeby.feature.servicelisting.domain.usecase.SortServicesUseCase
import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyRadiusSortIntegrationTest {

    private val filterUseCase = FilterServicesUseCase()
    private val locationProvider = MockLocationProvider()
    private val sortUseCase = SortServicesUseCase(locationProvider)

    @Test
    fun `radius filter excludes listings outside boundary`() {
        val listings = MockServiceDataSource.sampleListings
        val filter = ServiceFilter(radiusKm = RadiusOption.FIVE_KM)
        val filtered = filterUseCase(listings, filter)
        filtered.forEach { listing ->
            val km = listing.distanceInfo?.distanceKm
            assertTrue("expected <= 5km, was $km", km != null && km <= 5.0)
        }
    }

    @Test
    fun `nearest first sort places closest listing first`() = runTest {
        val listings = MockServiceDataSource.sampleListings
        val sorted = sortUseCase(listings, com.closeby.feature.servicelisting.domain.model.SortOption.NEAREST_FIRST)
        val distances = sorted.mapNotNull { it.distanceInfo?.distanceKm }
        assertEquals(distances, distances.sorted())
    }

    @Test
    fun `distance info formats meters and kilometers`() {
        val meters = DistanceInfo(distanceKm = 0.85, status = LocationStatus.AVAILABLE)
        assertEquals("850 m away", meters.formatted())

        val km = DistanceInfo(distanceKm = 2.4, status = LocationStatus.AVAILABLE)
        assertEquals("2.4 km away", km.formatted())
    }
}
