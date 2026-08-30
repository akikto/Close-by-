package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.mock.MockLocationProvider
import com.closeby.feature.servicelisting.data.mock.MockServiceDataSource
import com.closeby.feature.servicelisting.domain.model.SortOption
import com.closeby.feature.servicelisting.domain.usecase.SortServicesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the sorting CONTRACT: nearest-first sorting is delegated to the
 * [com.closeby.feature.servicelisting.domain.repository.LocationProvider]
 * rather than computed inside this module. Actual distance/geo math is
 * owned and tested by Agent 2's Location module, not here.
 */
class SortingContractTest {

    private val locationProvider = MockLocationProvider()
    private val useCase = SortServicesUseCase(locationProvider)
    private val listings = MockServiceDataSource.sampleListings

    @Test
    fun `default sort option is nearest first`() {
        assertEquals(SortOption.NEAREST_FIRST, SortOption.DEFAULT)
    }

    @Test
    fun `nearest first sort delegates to location provider and orders ascending`() = runTest {
        val result = useCase(listings, SortOption.NEAREST_FIRST)
        val distances = result.map { it.distanceInfo?.distanceKm ?: Double.MAX_VALUE }
        assertEquals(distances.sorted(), distances)
    }

    @Test
    fun `highest rated sort orders descending by rating`() = runTest {
        val result = useCase(listings, SortOption.HIGHEST_RATED)
        val ratings = result.map { it.rating }
        assertEquals(ratings.sortedDescending(), ratings)
    }

    @Test
    fun `lowest price sort orders ascending by price amount`() = runTest {
        val result = useCase(listings, SortOption.LOWEST_PRICE)
        val prices = result.map { it.price.amount }
        assertEquals(prices.sorted(), prices)
    }

    @Test
    fun `sorting does not drop or duplicate listings`() = runTest {
        val result = useCase(listings, SortOption.NEAREST_FIRST)
        assertEquals(listings.size, result.size)
        assertTrue(result.map { it.id }.toSet() == listings.map { it.id }.toSet())
    }
}
