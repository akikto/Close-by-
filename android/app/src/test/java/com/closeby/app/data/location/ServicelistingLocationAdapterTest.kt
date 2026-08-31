package com.closeby.app.data.location

import com.closeby.feature.nearby.location.LocationAvailability
import com.closeby.feature.nearby.location.LocationPermissionState
import com.closeby.feature.nearby.location.LocationProvider as DeviceLocationProvider
import com.closeby.feature.nearby.model.Coordinates
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.DistanceInfo
import com.closeby.feature.servicelisting.domain.model.LocationStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import com.closeby.feature.nearby.util.DistanceCalculator
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ServicelistingLocationAdapterTest {

    private lateinit var session: LocationSession
    private lateinit var adapter: ServicelistingLocationAdapter

    @Before
    fun setUp() {
        val fakeDevice = FakeDeviceLocationProvider(
            Coordinates(12.9716, 77.5946)
        )
        session = LocationSession(fakeDevice)
        adapter = ServicelistingLocationAdapter(session)
    }

    @Test
    fun `attachDistances computes haversine distance in km`() = runTest {
        session.bind(this)
        session.coordinates.filterNotNull().first()

        val listings = listOf(sampleListing(lat = 12.9716, lng = 77.6046))
        val enriched = adapter.attachDistances(listings)

        val km = enriched.first().distanceInfo!!.distanceKm!!
        assertTrue(km in 0.5..2.0)
    }

    @Test
    fun `sortNearestFirst orders ascending by distance`() = runTest {
        val near = sampleListing(id = "near", lat = 12.9716, lng = 77.5946)
            .copy(distanceInfo = DistanceInfo(0.5, LocationStatus.AVAILABLE))
        val far = sampleListing(id = "far", lat = 13.0, lng = 77.6)
            .copy(distanceInfo = DistanceInfo(5.0, LocationStatus.AVAILABLE))

        val sorted = adapter.sortNearestFirst(listOf(far, near))
        assertEquals("near", sorted.first().id)
    }

    private fun sampleListing(id: String = "1", lat: Double, lng: Double) = ServiceListing(
        id = id,
        providerId = "p1",
        category = ServiceCategory.EQUIPMENT,
        subcategory = ServiceSubcategory.WATER_PUMP,
        title = "Pump",
        description = "desc",
        latitude = lat,
        longitude = lng,
        availability = AvailabilityStatus.AVAILABLE_NOW,
        price = PriceInfo(100.0, PriceUnit.DAY),
        contactNumber = "+91123",
        providerName = "Test"
    )

    private class FakeDeviceLocationProvider(
        private val coords: Coordinates
    ) : DeviceLocationProvider {
        override fun observeLocation(): Flow<LocationAvailability> = flow {
            emit(LocationAvailability.Available(coords))
        }

        override fun currentPermissionState() = LocationPermissionState.Granted
        override fun isLocationServiceEnabled() = true
        override fun retry() = Unit
    }
}
