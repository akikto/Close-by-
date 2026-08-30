package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class SubcategoryFilterTest {

    private val useCase = FilterServicesUseCase()
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `subcategory filter narrows to exact subcategory only`() {
        val result = useCase(listings, ServiceFilter(subcategory = ServiceSubcategory.ELECTRICIAN))
        assertEquals(1, result.size)
        assertEquals(ServiceSubcategory.ELECTRICIAN, result.first().subcategory)
    }

    @Test
    fun `subcategory filter with no matches returns empty list`() {
        val result = useCase(listings, ServiceFilter(subcategory = ServiceSubcategory.BUS))
        assertEquals(0, result.size)
    }
}
