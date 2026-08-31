package com.closeby.feature.provider

import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.availability.domain.validation.AvailabilityValidator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class AvailabilityOverlapTest {

    @Test
    fun duplicateDayFails() {
        val entries = listOf(
            ProviderAvailability("p1", DayOfWeek.MONDAY, true, LocalTime.of(8, 0), LocalTime.of(12, 0)),
            ProviderAvailability("p1", DayOfWeek.MONDAY, true, LocalTime.of(13, 0), LocalTime.of(17, 0))
        )
        assertTrue(AvailabilityValidator.validateAll(entries).isFailure)
    }
}
