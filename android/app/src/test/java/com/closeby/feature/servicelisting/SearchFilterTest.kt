package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.usecase.SearchServicesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFilterTest {

    private val useCase = SearchServicesUseCase()
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `blank query returns all listings`() {
        val result = useCase(listings, "")
        assertEquals(listings.size, result.size)
    }

    @Test
    fun `search matches by title`() {
        val result = useCase(listings, "Water Pump")
        assertTrue(result.all { it.title.contains("Water Pump", ignoreCase = true) })
        assertEquals(1, result.size)
    }

    @Test
    fun `search matches by category name`() {
        val result = useCase(listings, "Labour")
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.category.displayName.equals("Labour", ignoreCase = true) })
    }

    @Test
    fun `search matches by subcategory name`() {
        val result = useCase(listings, "Electrician")
        assertEquals(1, result.size)
    }

    @Test
    fun `search matches by provider name`() {
        val result = useCase(listings, "Farm Equip")
        assertEquals(1, result.size)
    }

    @Test
    fun `search is case insensitive`() {
        val result = useCase(listings, "tractor")
        assertEquals(1, result.size)
    }
}
