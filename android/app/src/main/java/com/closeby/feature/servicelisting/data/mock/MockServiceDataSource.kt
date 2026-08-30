package com.closeby.feature.servicelisting.data.mock

import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.DistanceInfo
import com.closeby.feature.servicelisting.domain.model.LocationStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import com.closeby.feature.servicelisting.domain.repository.LocationProvider
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ⚠️ TEMPORARY MOCK DATA — for UI previews and unit tests only.
 * Contains no production credentials and is NOT wired to Supabase or any
 * real backend. Replace with a real [ServiceRepository] implementation
 * (e.g. Supabase-backed) at the app composition/DI layer.
 */
object MockServiceDataSource {

    val sampleListings: List<ServiceListing> = listOf(
        ServiceListing(
            id = "svc_001",
            providerId = "provider_101",
            category = ServiceCategory.EQUIPMENT,
            subcategory = ServiceSubcategory.WATER_PUMP,
            title = "Water Pump",
            description = "High-capacity water pump suitable for agricultural and construction use.",
            imageUrls = listOf("https://example.com/mock/water_pump.jpg"),
            latitude = 12.9716,
            longitude = 77.5946,
            availability = AvailabilityStatus.AVAILABLE_NOW,
            price = PriceInfo(amount = 500.0, unit = PriceUnit.DAY),
            contactNumber = "+910000000001",
            providerName = "Ravi Kumar",
            rating = 4.7,
            reviewCount = 32,
            isVerifiedProvider = true,
            distanceInfo = DistanceInfo(distanceKm = 1.2, status = LocationStatus.AVAILABLE)
        ),
        ServiceListing(
            id = "svc_002",
            providerId = "provider_102",
            category = ServiceCategory.LABOUR,
            subcategory = ServiceSubcategory.ELECTRICIAN,
            title = "Experienced Electrician",
            description = "Residential and commercial electrical work, wiring, and repairs.",
            imageUrls = listOf("https://example.com/mock/electrician.jpg"),
            latitude = 12.9352,
            longitude = 77.6146,
            availability = AvailabilityStatus.AVAILABLE_SOON,
            price = PriceInfo(amount = 300.0, unit = PriceUnit.HOUR),
            contactNumber = "+910000000002",
            providerName = "Suresh Electricals",
            rating = 4.5,
            reviewCount = 18,
            isVerifiedProvider = false,
            distanceInfo = DistanceInfo(distanceKm = 3.4, status = LocationStatus.AVAILABLE)
        ),
        ServiceListing(
            id = "svc_003",
            providerId = "provider_103",
            category = ServiceCategory.VEHICLES,
            subcategory = ServiceSubcategory.TRACTOR,
            title = "Mahindra Tractor for Hire",
            description = "Well maintained tractor available for farm and transport work.",
            imageUrls = listOf("https://example.com/mock/tractor.jpg"),
            latitude = 13.0, longitude = 77.6,
            availability = AvailabilityStatus.AVAILABLE_NOW,
            price = PriceInfo(amount = 1500.0, unit = PriceUnit.TRIP),
            contactNumber = "+910000000003",
            providerName = "Farm Equip Co.",
            rating = 4.9,
            reviewCount = 51,
            isVerifiedProvider = true,
            distanceInfo = DistanceInfo(distanceKm = 8.1, status = LocationStatus.AVAILABLE)
        )
    )
}

/** ⚠️ TEMPORARY mock repository — swap for the real backend-backed implementation. */
class MockServiceRepository : ServiceRepository {
    private val state = MutableStateFlow(MockServiceDataSource.sampleListings)

    override fun observeServices(): Flow<List<ServiceListing>> = state.asStateFlow()

    override suspend fun fetchServices(): Result<List<ServiceListing>> =
        Result.success(state.value)

    override suspend fun getServiceById(id: String): Result<ServiceListing> =
        state.value.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("No listing found for id=$id"))
}

/**
 * ⚠️ TEMPORARY mock LocationProvider — a stand-in for Agent 2's real
 * Location + Nearby module. Performs no real distance math; simply passes
 * through whatever `distanceInfo` mock listings already carry and sorts by
 * it. Replace this with the real implementation at integration time.
 */
class MockLocationProvider : LocationProvider {
    private val statusFlow = MutableStateFlow(LocationStatus.AVAILABLE)

    override fun observeLocationStatus(): Flow<LocationStatus> = statusFlow.asStateFlow()

    override suspend fun attachDistances(listings: List<ServiceListing>): List<ServiceListing> = listings

    override suspend fun sortNearestFirst(listings: List<ServiceListing>): List<ServiceListing> =
        listings.sortedBy { it.distanceInfo?.distanceKm ?: Double.MAX_VALUE }
}
