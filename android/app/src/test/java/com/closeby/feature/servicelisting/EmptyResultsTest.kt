package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.model.RadiusOption
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import com.closeby.feature.servicelisting.domain.usecase.SearchServicesUseCase
import org.junit.Assert.assertTrue
import org.junit.Test

class EmptyResultsTest {

    private val searchUseCase = SearchServicesUseCase()
    private val filterUseCase = FilterServicesUseCase()
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `search with nonsense query returns no search results`() {
        val result = searchUseCase(listings, "zzz_no_such_service_zzz")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty source list produces empty results regardless of filter`() {
        val result = filterUseCase(emptyList(), ServiceFilter())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `radius filter with no distance info excludes listing`() {
        val listingWithoutDistance = listings.first().copy(distanceInfo = null)
        val result = filterUseCase(listOf(listingWithoutDistance), ServiceFilter(radiusKm = RadiusOption.ONE_KM))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `radius filter excludes listings beyond the selected radius`() {
        val farListing = listings.first().copy(
            distanceInfo = listings.first().distanceInfo!!.copy(distanceKm = 50.0)
        )
        val result = filterUseCase(listOf(farListing), ServiceFilter(radiusKm = RadiusOption.ONE_KM))
        assertTrue(result.isEmpty())
    }
}
