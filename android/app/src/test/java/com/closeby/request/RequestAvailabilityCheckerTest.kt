package com.closeby.request

import com.closeby.availability.data.repository.MockAvailabilityRepository
import com.closeby.request.domain.validation.RequestAvailabilityChecker
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RequestAvailabilityCheckerTest {

    private val repository = MockAvailabilityRepository()
    private val providerId = "11111111-1111-1111-1111-111111111101"

    @Test
    fun validTimeWithinProviderWindowPasses() = runTest {
        val monday = LocalDate.of(2026, 9, 7) // Monday
        val result = RequestAvailabilityChecker.validateProviderAvailable(
            repository,
            providerId,
            monday,
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun timeOutsideProviderWindowFails() = runTest {
        val monday = LocalDate.of(2026, 9, 7)
        val result = RequestAvailabilityChecker.validateProviderAvailable(
            repository,
            providerId,
            monday,
            LocalTime.of(19, 0),
            LocalTime.of(21, 0)
        )
        assertTrue(result.isFailure)
    }
}
