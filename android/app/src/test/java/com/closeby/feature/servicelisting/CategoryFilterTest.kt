package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryFilterTest {

    private val useCase = FilterServicesUseCase()
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `filtering by category returns only that category`() {
        val result = useCase(listings, ServiceFilter(category = ServiceCategory.VEHICLES))
        assertTrue(result.all { it.category == ServiceCategory.VEHICLES })
    }

    @Test
    fun `filtering by subcategory returns only that subcategory`() {
        val result = useCase(listings, ServiceFilter(subcategory = ServiceSubcategory.TRACTOR))
        assertTrue(result.all { it.subcategory == ServiceSubcategory.TRACTOR })
        assertEquals(1, result.size)
    }

    @Test
    fun `no category filter returns all listings`() {
        val result = useCase(listings, ServiceFilter())
        assertEquals(listings.size, result.size)
    }
}
