package com.closeby.app.data.mapper

import com.closeby.app.data.model.ProviderEmbedDto
import com.closeby.app.data.model.ServiceDto
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ServiceListingMapperTest {

    @Test
    fun `maps valid service dto to domain model`() {
        val dto = ServiceDto(
            id = "svc-1",
            providerId = "prov-1",
            category = "EQUIPMENT",
            subcategory = "WATER_PUMP",
            title = "Water Pump",
            description = "Test pump",
            imageUrls = listOf("https://example.com/pump.jpg"),
            latitude = 12.97,
            longitude = 77.59,
            availability = "AVAILABLE_NOW",
            priceAmount = 500.0,
            priceUnit = "DAY",
            priceIsStarting = false,
            rating = 4.5,
            reviewCount = 10,
            providers = ProviderEmbedDto(
                name = "Ravi Kumar",
                phoneNumber = "+910000000001",
                isVerified = true
            )
        )

        val listing = ServiceListingMapper.toDomainOrNull(dto)

        assertNotNull(listing)
        assertEquals("svc-1", listing!!.id)
        assertEquals(ServiceCategory.EQUIPMENT, listing.category)
        assertEquals(ServiceSubcategory.WATER_PUMP, listing.subcategory)
        assertEquals(AvailabilityStatus.AVAILABLE_NOW, listing.availability)
        assertEquals(PriceUnit.DAY, listing.price.unit)
        assertEquals("Ravi Kumar", listing.providerName)
        assertEquals(true, listing.isVerifiedProvider)
    }

    @Test
    fun `returns null for invalid category`() {
        val dto = sampleDto(category = "INVALID")

        assertNull(ServiceListingMapper.toDomainOrNull(dto))
    }

    @Test
    fun `returns null when provider embed is missing`() {
        val dto = sampleDto().copy(providers = null)

        assertNull(ServiceListingMapper.toDomainOrNull(dto))
    }

    @Test
    fun `toDomainList skips invalid rows`() {
        val valid = sampleDto(id = "valid")
        val invalid = sampleDto(id = "invalid", category = "BAD")

        val result = ServiceListingMapper.toDomainList(listOf(valid, invalid))

        assertEquals(1, result.size)
        assertEquals("valid", result.first().id)
    }

    private fun sampleDto(
        id: String = "svc-1",
        category: String = "LABOUR"
    ) = ServiceDto(
        id = id,
        providerId = "prov-1",
        category = category,
        subcategory = "ELECTRICIAN",
        title = "Electrician",
        description = "Wiring",
        latitude = 12.0,
        longitude = 77.0,
        priceAmount = 300.0,
        priceUnit = "HOUR",
        providers = ProviderEmbedDto(
            name = "Suresh",
            phoneNumber = "+910000000002"
        )
    )
}
