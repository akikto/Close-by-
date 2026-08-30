package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceModelTest {

    private fun sampleListing(price: PriceInfo) = ServiceListing(
        id = "1",
        providerId = "p1",
        category = ServiceCategory.EQUIPMENT,
        subcategory = ServiceSubcategory.WATER_PUMP,
        title = "Water Pump",
        description = "desc",
        latitude = 0.0,
        longitude = 0.0,
        availability = AvailabilityStatus.AVAILABLE_NOW,
        price = price,
        contactNumber = "+911234567890",
        providerName = "Provider"
    )

    @Test
    fun `price formats with unit`() {
        val price = PriceInfo(amount = 500.0, unit = PriceUnit.DAY)
        assertEquals("₹500 / Day", price.formatted())
    }

    @Test
    fun `starting price formats without unit suffix`() {
        val price = PriceInfo(amount = 800.0, unit = PriceUnit.TRIP, isStartingPrice = true)
        assertEquals("Starting ₹800", price.formatted())
    }

    @Test
    fun `subcategory belongs to its declared parent category`() {
        assertEquals(ServiceCategory.EQUIPMENT, ServiceSubcategory.WATER_PUMP.parent)
        assertTrue(ServiceCategory.EQUIPMENT.subcategories().contains(ServiceSubcategory.WATER_PUMP))
    }

    @Test
    fun `listing carries required fields`() {
        val listing = sampleListing(PriceInfo(500.0, PriceUnit.DAY))
        assertEquals("Water Pump", listing.title)
        assertEquals(ServiceCategory.EQUIPMENT, listing.category)
    }
}
