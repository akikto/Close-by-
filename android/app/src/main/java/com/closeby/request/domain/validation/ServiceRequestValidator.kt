package com.closeby.request.domain.validation

import com.closeby.request.domain.model.ServiceRequestStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * Validation errors surfaced to the UI layer (CreateServiceRequestViewModel etc.)
 * as human-readable [message]s.
 */
sealed class ServiceRequestValidationError(message: String) : Exception(message) {
    data object EmptyServiceTitle : ServiceRequestValidationError("Service name is required.")
    data object PastDate : ServiceRequestValidationError("Requested date cannot be in the past.")
    data object EndBeforeOrEqualStart :
        ServiceRequestValidationError("End time must be after start time.")
    data object NegativeBudget :
        ServiceRequestValidationError("Budget cannot be negative.")
    data class InvalidStatusTransition(
        val from: ServiceRequestStatus,
        val to: ServiceRequestStatus
    ) : ServiceRequestValidationError("Cannot change status from $from to $to.")
}

/**
 * Stateless domain validation. Kept dependency-free (no Android, no
 * repository) so it is trivially unit-testable and reusable by both the
 * ViewModels and repository implementations.
 */
object ServiceRequestValidator {

    fun validateServiceTitle(title: String): Result<Unit> =
        if (title.isBlank()) {
            Result.failure(ServiceRequestValidationError.EmptyServiceTitle)
        } else {
            Result.success(Unit)
        }

    fun validateDate(date: LocalDate, today: LocalDate = LocalDate.now()): Result<Unit> =
        if (date.isBefore(today)) {
            Result.failure(ServiceRequestValidationError.PastDate)
        } else {
            Result.success(Unit)
        }

    /**
     * Rejects end time before OR equal to start time — zero-duration
     * requests are not allowed per the contract.
     */
    fun validateTimeRange(startTime: LocalTime, endTime: LocalTime): Result<Unit> =
        if (!endTime.isAfter(startTime)) {
            Result.failure(ServiceRequestValidationError.EndBeforeOrEqualStart)
        } else {
            Result.success(Unit)
        }

    fun validateBudget(budgetAmount: Double?): Result<Unit> =
        if (budgetAmount != null && budgetAmount < 0.0) {
            Result.failure(ServiceRequestValidationError.NegativeBudget)
        } else {
            Result.success(Unit)
        }

    /** Runs every field validation and returns the first failure, if any. */
    fun validateNewRequest(
        serviceTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        budgetAmount: Double?,
        today: LocalDate = LocalDate.now()
    ): Result<Unit> {
        validateServiceTitle(serviceTitle).onFailure { return Result.failure(it) }
        validateDate(date, today).onFailure { return Result.failure(it) }
        validateTimeRange(startTime, endTime).onFailure { return Result.failure(it) }
        validateBudget(budgetAmount).onFailure { return Result.failure(it) }
        return Result.success(Unit)
    }

    /** Validates a status transition against [ServiceRequestStatus.canTransitionTo]. */
    fun validateStatusTransition(
        from: ServiceRequestStatus,
        to: ServiceRequestStatus
    ): Result<Unit> =
        if (from.canTransitionTo(to)) {
            Result.success(Unit)
        } else {
            Result.failure(ServiceRequestValidationError.InvalidStatusTransition(from, to))
        }
}
