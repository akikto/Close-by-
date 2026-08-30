package com.closeby.feature.nearby.domain

import com.closeby.feature.nearby.model.Coordinates
import com.closeby.feature.nearby.model.ServiceLocation
import com.closeby.feature.nearby.util.DistanceCalculator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNearbyServicesUseCaseTest {

    private val userLocation = Coordinates(37.7749, -122.4194) // SF downtown

    /** Builds a ServiceLocation at an exact distance (km) due north of userLocation. */
    private fun locationAtDistanceKm(id: String, km: Double): ServiceLocation {
        val kmPerDegreeLat = 111.32
        val lat = userLocation.latitude + (km / kmPerDegreeLat)
        return ServiceLocation(serviceId = id, latitude = lat, longitude = userLocation.longitude)
    }

    @Test
    fun `nearest-first sorting ascending`() = runBlocking {
        val far = locationAtDistanceKm("far", 6.8)
        val mid = locationAtDistanceKm("mid", 3.2)
        val near = locationAtDistanceKm("near", 1.4)
        val nearest = locationAtDistanceKm("nearest", 0.8)

        // Deliberately inserted out of order.
        val repo = FakeNearbyServiceRepository(locations = listOf(far, near, nearest, mid))
        val useCase = GetNearbyServicesUseCase(repo)

        val result = useCase(NearbySearchParams(userLocation, radiusKm = 25.0)).getOrThrow()

        assertEquals(listOf("nearest", "near", "mid", "far"), result.map { it.serviceId })
        // Strictly ascending.
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].distanceMeters <= result[i + 1].distanceMeters)
        }
    }

    @Test
    fun `radius filtering excludes services outside radius`() = runBlocking {
        val inside = locationAtDistanceKm("inside", 3.0)
        val outside = locationAtDistanceKm("outside", 12.0)

        val repo = FakeNearbyServiceRepository(locations = listOf(inside, outside))
        val useCase = GetNearbyServicesUseCase(repo)

        val result = useCase(NearbySearchParams(userLocation, radiusKm = 5.0)).getOrThrow()

        assertEquals(listOf("inside"), result.map { it.serviceId })
    }

    @Test
    fun `boundary distance exactly at radius is included`() = runBlocking {
        // Build a location, measure its exact distance, then use that exact value as radius.
        val candidate = locationAtDistanceKm("boundary", 10.0)
        val exactKm = DistanceCalculator.distanceKilometers(userLocation, candidate.toCoordinates())

        val repo = FakeNearbyServiceRepository(locations = listOf(candidate))
        val useCase = GetNearbyServicesUseCase(repo)

        val result = useCase(NearbySearchParams(userLocation, radiusKm = exactKm)).getOrThrow()

        assertEquals(listOf("boundary"), result.map { it.serviceId })
    }

    @Test
    fun `boundary distance just outside radius is excluded`() = runBlocking {
        val candidate = locationAtDistanceKm("just-outside", 10.0)
        val exactKm = DistanceCalculator.distanceKilometers(userLocation, candidate.toCoordinates())

        val repo = FakeNearbyServiceRepository(locations = listOf(candidate))
        val useCase = GetNearbyServicesUseCase(repo)

        val result = useCase(NearbySearchParams(userLocation, radiusKm = exactKm - 0.01)).getOrThrow()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty candidate list yields empty results, not an error`() = runBlocking {
        val repo = FakeNearbyServiceRepository(locations = emptyList())
        val useCase = GetNearbyServicesUseCase(repo)

        val result = useCase(NearbySearchParams(userLocation, radiusKm = 10.0))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `all candidates outside radius yields empty results`() = runBlocking {
        val far1 = locationAtDistanceKm("far1", 50.0)
        val far2 = locationAtDistanceKm("far2", 100.0)
        val repo = FakeNearbyServiceRepository(locations = listOf(far1, far2))
        val useCase = GetNearbyServicesUseCase(repo)

        val result = useCase(NearbySearchParams(userLocation, radiusKm = 10.0)).getOrThrow()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `repository failure surfaces as Result failure, not a crash`() = runBlocking {
        val repo = FakeNearbyServiceRepository(failure = IllegalStateException("network down"))
        val useCase = GetNearbyServicesUseCase(repo)

        val result = useCase(NearbySearchParams(userLocation, radiusKm = 10.0))

        assertTrue(result.isFailure)
        assertEquals("network down", result.exceptionOrNull()?.message)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid non-positive radius is rejected`() {
        NearbySearchParams(userLocation, radiusKm = 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid latitude out of range is rejected`() {
        Coordinates(latitude = 500.0, longitude = 0.0)
    }
}
