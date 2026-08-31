package com.closeby.feature.provider

import com.closeby.app.data.model.ProviderDto
import com.closeby.feature.provider.data.mapper.ProviderProfileMapper
import com.closeby.feature.provider.domain.model.ManagedServiceSummary
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ProviderProfileMapperTest {

    @Test
    fun mapsProviderProfileWithoutExposingCoordinates() {
        val profile = ProviderProfileMapper.toProfile(
            provider = ProviderDto(
                id = "p1",
                name = "Ravi",
                category = "EQUIPMENT",
                phoneNumber = "+910000000001",
                latitude = 12.97,
                longitude = 77.59,
                isVerified = true,
                rating = 4.5,
                reviewCount = 10
            ),
            services = listOf(
                ManagedServiceSummary(
                    id = "s1",
                    title = "Pump",
                    category = ServiceCategory.EQUIPMENT,
                    subcategory = ServiceSubcategory.WATER_PUMP,
                    price = PriceInfo(500.0, PriceUnit.DAY),
                    availability = AvailabilityStatus.AVAILABLE_NOW,
                    isActive = true
                )
            ),
            availability = emptyList(),
            distanceLabel = "2.4 km away",
            isOwnProfile = false
        )

        assertNotNull(profile)
        assertEquals("2.4 km away", profile!!.distanceLabel)
        assertNull(profile.phoneNumber)
    }
}
