package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.model.AvailabilityFilter
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityFilterTest {

    private val useCase = FilterServicesUseCase()
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `available now filter only returns available now listings`() {
        val result = useCase(listings, ServiceFilter(availability = AvailabilityFilter.AVAILABLE_NOW))
        assertTrue(result.all { it.availability == AvailabilityStatus.AVAILABLE_NOW })
    }

    @Test
    fun `any availability returns all listings`() {
        val result = useCase(listings, ServiceFilter(availability = AvailabilityFilter.ANY))
        assertTrue(result.size == listings.size)
    }
}
