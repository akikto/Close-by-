package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingFilterTest {

    private val useCase = FilterServicesUseCase()
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `4 star and above filter excludes lower rated listings`() {
        val result = useCase(listings, ServiceFilter(minRating = 4.0))
        assertTrue(result.all { it.rating >= 4.0 })
    }

    @Test
    fun `no matches for unrealistically high rating`() {
        val result = useCase(listings, ServiceFilter(minRating = 5.1))
        assertTrue(result.isEmpty())
    }
}
