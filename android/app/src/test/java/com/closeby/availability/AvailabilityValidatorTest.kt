package com.closeby.availability

import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.availability.domain.validation.AvailabilityValidator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class AvailabilityValidatorTest {

    @Test
    fun `not available day requires no time range`() {
        val entry = ProviderAvailability(
            providerId = "p1",
            dayOfWeek = DayOfWeek.SUNDAY,
            isAvailable = false
        )
        assertTrue(AvailabilityValidator.validate(entry).isSuccess)
    }

    @Test
    fun `available day with valid time range passes`() {
        val entry = ProviderAvailability(
            providerId = "p1",
            dayOfWeek = DayOfWeek.MONDAY,
            isAvailable = true,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(18, 0)
        )
        assertTrue(AvailabilityValidator.validate(entry).isSuccess)
    }

    @Test
    fun `available day missing times fails`() {
        val entry = ProviderAvailability(
            providerId = "p1",
            dayOfWeek = DayOfWeek.MONDAY,
            isAvailable = true,
            startTime = null,
            endTime = null
        )
        assertTrue(AvailabilityValidator.validate(entry).isFailure)
    }

    @Test
    fun `end time before start time fails`() {
        val result = AvailabilityValidator.validateTimeRange(
            LocalTime.of(17, 0),
            LocalTime.of(9, 0)
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `end time equal to start time fails (zero duration)`() {
        val result = AvailabilityValidator.validateTimeRange(
            LocalTime.of(9, 0),
            LocalTime.of(9, 0)
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `end time after start time passes`() {
        val result = AvailabilityValidator.validateTimeRange(
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateAll fails fast on first invalid entry`() {
        val entries = listOf(
            ProviderAvailability("p1", DayOfWeek.MONDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0)),
            ProviderAvailability("p1", DayOfWeek.WEDNESDAY, false),
            ProviderAvailability("p1", DayOfWeek.TUESDAY, true, LocalTime.of(18, 0), LocalTime.of(8, 0))
        )
        assertTrue(AvailabilityValidator.validateAll(entries).isFailure)
    }

    @Test
    fun `validateAll passes for the contract's example week`() {
        val entries = listOf(
            ProviderAvailability("p1", DayOfWeek.MONDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0)),
            ProviderAvailability("p1", DayOfWeek.TUESDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0)),
            ProviderAvailability("p1", DayOfWeek.WEDNESDAY, false),
            ProviderAvailability("p1", DayOfWeek.THURSDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0)),
            ProviderAvailability("p1", DayOfWeek.FRIDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0)),
            ProviderAvailability("p1", DayOfWeek.SATURDAY, true, LocalTime.of(8, 0), LocalTime.of(18, 0)),
            ProviderAvailability("p1", DayOfWeek.SUNDAY, false)
        )
        assertTrue(AvailabilityValidator.validateAll(entries).isSuccess)
    }
}
