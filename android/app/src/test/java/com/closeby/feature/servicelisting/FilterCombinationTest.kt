package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.model.AvailabilityFilter
import com.closeby.feature.servicelisting.domain.model.RadiusOption
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import com.closeby.feature.servicelisting.domain.usecase.SearchServicesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterCombinationTest {

    private val searchUseCase = SearchServicesUseCase()
    private val filterUseCase = FilterServicesUseCase()
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `category plus rating filter combine with AND semantics`() {
        val filter = ServiceFilter(category = ServiceCategory.EQUIPMENT, minRating = 4.0)
        val result = filterUseCase(listings, filter)
        assertTrue(result.all { it.category == ServiceCategory.EQUIPMENT && it.rating >= 4.0 })
    }

    @Test
    fun `search plus filter narrows further than either alone`() {
        val searched = searchUseCase(listings, "Pump")
        val filtered = filterUseCase(searched, ServiceFilter(minRating = 4.5))
        assertTrue(filtered.size <= searched.size)
        assertTrue(filtered.all { it.rating >= 4.5 })
    }

    @Test
    fun `combining radius availability and rating filters`() {
        val filter = ServiceFilter(
            radiusKm = RadiusOption.TEN_KM,
            availability = AvailabilityFilter.AVAILABLE_NOW,
            minRating = 4.0
        )
        val result = filterUseCase(listings, filter)
        assertTrue(
            result.all {
                (it.distanceInfo?.distanceKm ?: Double.MAX_VALUE) <= 10.0 &&
                    it.rating >= 4.0
            }
        )
    }

    @Test
    fun `empty filter returns unfiltered set`() {
        val filter = ServiceFilter()
        assertEquals(listings.size, filterUseCase(listings, filter).size)
        assertTrue(filter.isEmpty)
    }
}
