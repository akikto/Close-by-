package com.closeby.request

import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.validation.ServiceRequestValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ServiceRequestValidatorTest {

    private val today = LocalDate.of(2026, 8, 30)

    // --- request creation / field validation -------------------------------

    @Test
    fun `valid request passes all validation`() {
        val result = ServiceRequestValidator.validateNewRequest(
            serviceTitle = "Water Pump",
            date = today,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            budgetAmount = 400.0,
            today = today
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `blank service title fails validation`() {
        val result = ServiceRequestValidator.validateServiceTitle("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `past date is invalid`() {
        val result = ServiceRequestValidator.validateDate(today.minusDays(1), today)
        assertTrue(result.isFailure)
    }

    @Test
    fun `today's date is valid`() {
        val result = ServiceRequestValidator.validateDate(today, today)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `end time before start time is invalid`() {
        val result = ServiceRequestValidator.validateTimeRange(
            LocalTime.of(17, 0),
            LocalTime.of(9, 0)
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `end time equal to start time is invalid (zero duration)`() {
        val result = ServiceRequestValidator.validateTimeRange(
            LocalTime.of(9, 0),
            LocalTime.of(9, 0)
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `end time after start time is valid`() {
        val result = ServiceRequestValidator.validateTimeRange(
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `negative budget is invalid`() {
        val result = ServiceRequestValidator.validateBudget(-100.0)
        assertTrue(result.isFailure)
    }

    @Test
    fun `null budget is valid (budget is optional)`() {
        val result = ServiceRequestValidator.validateBudget(null)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `zero budget is valid`() {
        val result = ServiceRequestValidator.validateBudget(0.0)
        assertTrue(result.isSuccess)
    }

    // --- status transitions --------------------------------------------------

    @Test
    fun `pending to accepted is valid`() {
        assertTrue(ServiceRequestStatus.PENDING.canTransitionTo(ServiceRequestStatus.ACCEPTED))
    }

    @Test
    fun `pending to rejected is valid`() {
        assertTrue(ServiceRequestStatus.PENDING.canTransitionTo(ServiceRequestStatus.REJECTED))
    }

    @Test
    fun `pending to cancelled is valid`() {
        assertTrue(ServiceRequestStatus.PENDING.canTransitionTo(ServiceRequestStatus.CANCELLED))
    }

    @Test
    fun `accepted to completed is valid`() {
        assertTrue(ServiceRequestStatus.ACCEPTED.canTransitionTo(ServiceRequestStatus.COMPLETED))
    }

    @Test
    fun `accepted to cancelled is valid`() {
        assertTrue(ServiceRequestStatus.ACCEPTED.canTransitionTo(ServiceRequestStatus.CANCELLED))
    }

    @Test
    fun `rejected to accepted is invalid`() {
        assertFalse(ServiceRequestStatus.REJECTED.canTransitionTo(ServiceRequestStatus.ACCEPTED))
    }

    @Test
    fun `completed to pending is invalid`() {
        assertFalse(ServiceRequestStatus.COMPLETED.canTransitionTo(ServiceRequestStatus.PENDING))
    }

    @Test
    fun `cancelled to accepted is invalid`() {
        assertFalse(ServiceRequestStatus.CANCELLED.canTransitionTo(ServiceRequestStatus.ACCEPTED))
    }

    @Test
    fun `pending to completed is invalid (must go through accepted)`() {
        assertFalse(ServiceRequestStatus.PENDING.canTransitionTo(ServiceRequestStatus.COMPLETED))
    }

    @Test
    fun `validateStatusTransition returns failure for invalid transition`() {
        val result = ServiceRequestValidator.validateStatusTransition(
            ServiceRequestStatus.REJECTED,
            ServiceRequestStatus.ACCEPTED
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `validateStatusTransition returns success for valid transition`() {
        val result = ServiceRequestValidator.validateStatusTransition(
            ServiceRequestStatus.PENDING,
            ServiceRequestStatus.ACCEPTED
        )
        assertTrue(result.isSuccess)
    }
}
