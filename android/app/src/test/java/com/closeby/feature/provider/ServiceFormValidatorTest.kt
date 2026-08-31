package com.closeby.feature.provider

import com.closeby.feature.provider.domain.model.ServiceFormInput
import com.closeby.feature.provider.domain.validation.ServiceFormValidator
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceFormValidatorTest {

    private fun validInput() = ServiceFormInput(
        category = ServiceCategory.EQUIPMENT,
        subcategory = ServiceSubcategory.WATER_PUMP,
        title = "Water Pump",
        description = "Reliable pump for farm use.",
        latitude = 12.97,
        longitude = 77.59,
        availability = AvailabilityStatus.AVAILABLE_NOW,
        contactNumber = "+910000000001",
        priceAmount = 500.0,
        priceUnit = PriceUnit.DAY
    )

    @Test
    fun validFormPasses() {
        assertTrue(ServiceFormValidator.validate(validInput()).isSuccess)
    }

    @Test
    fun emptyTitleFails() {
        val result = ServiceFormValidator.validate(validInput().copy(title = "  "))
        assertTrue(result.isFailure)
    }

    @Test
    fun subcategoryMismatchFails() {
        val result = ServiceFormValidator.validate(
            validInput().copy(
                category = ServiceCategory.VEHICLES,
                subcategory = ServiceSubcategory.WATER_PUMP
            )
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun invalidPriceFails() {
        val result = ServiceFormValidator.validate(validInput().copy(priceAmount = -1.0))
        assertTrue(result.isFailure)
    }

    @Test
    fun priceWithoutUnitFails() {
        val result = ServiceFormValidator.validate(validInput().copy(priceUnit = null))
        assertTrue(result.isFailure)
    }

    @Test
    fun invalidPhoneFails() {
        val result = ServiceFormValidator.validate(validInput().copy(contactNumber = "abc"))
        assertFalse(result.isSuccess)
    }
}
