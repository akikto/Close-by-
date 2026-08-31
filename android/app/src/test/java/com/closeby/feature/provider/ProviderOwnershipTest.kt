package com.closeby.feature.provider

import com.closeby.feature.provider.data.repository.MockProviderManagementRepository
import com.closeby.feature.provider.domain.model.ServiceFormInput
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderOwnershipTest {

    private val repository = MockProviderManagementRepository()
    private val providerId = "11111111-1111-1111-1111-111111111101"
    private val otherProvider = "other-provider"

    @Test
    fun providerCannotEditAnotherProvidersService() = runTest {
        val serviceId = "22222222-2222-2222-2222-222222222201"
        val input = ServiceFormInput(
            category = ServiceCategory.EQUIPMENT,
            subcategory = ServiceSubcategory.WATER_PUMP,
            title = "Hack",
            description = "Nope",
            latitude = 1.0,
            longitude = 1.0,
            availability = AvailabilityStatus.AVAILABLE_NOW,
            contactNumber = "+910000000099"
        )
        val result = repository.updateService(serviceId, otherProvider, input)
        assertTrue(result.isFailure)
    }

    @Test
    fun providerCanToggleOwnService() = runTest {
        val serviceId = "22222222-2222-2222-2222-222222222201"
        val disable = repository.setServiceActive(serviceId, providerId, false)
        assertTrue(disable.isSuccess)
        val enable = repository.setServiceActive(serviceId, providerId, true)
        assertTrue(enable.isSuccess)
    }

    @Test
    fun providerCannotDeleteAnotherProvidersService() = runTest {
        val result = repository.deleteService(
            "22222222-2222-2222-2222-222222222201",
            otherProvider
        )
        assertTrue(result.isFailure)
    }
}
