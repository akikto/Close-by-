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
    data object EmptyDuration :
        ServiceRequestValidationError("Duration is required.")
    data object NoteTooLong :
        ServiceRequestValidationError("Note is too long (max 500 characters).")
    data object ProviderUnavailable :
        ServiceRequestValidationError("Provider is not available at the requested time.")
    data object ServiceInactive :
        ServiceRequestValidationError("This service is no longer available.")
    data object MissingContact :
        ServiceRequestValidationError("Name and phone are required to send a request.")
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

    const val MAX_NOTE_LENGTH = 500

    fun validateDuration(duration: String): Result<Unit> =
        if (duration.isBlank()) {
            Result.failure(ServiceRequestValidationError.EmptyDuration)
        } else {
            Result.success(Unit)
        }

    fun validateNote(note: String?): Result<Unit> {
        if (note != null && note.length > MAX_NOTE_LENGTH) {
            return Result.failure(ServiceRequestValidationError.NoteTooLong)
        }
        return Result.success(Unit)
    }

    fun validateAnonymousContact(customerName: String?, customerPhone: String?): Result<Unit> {
        val nameOk = !customerName.isNullOrBlank()
        val phoneOk = !customerPhone.isNullOrBlank() && customerPhone.any { it.isDigit() }
        return if (nameOk && phoneOk) Result.success(Unit)
        else Result.failure(ServiceRequestValidationError.MissingContact)
    }

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
        duration: String,
        budgetAmount: Double?,
        note: String? = null,
        today: LocalDate = LocalDate.now()
    ): Result<Unit> {
        validateServiceTitle(serviceTitle).onFailure { return Result.failure(it) }
        validateDate(date, today).onFailure { return Result.failure(it) }
        validateTimeRange(startTime, endTime).onFailure { return Result.failure(it) }
        validateDuration(duration).onFailure { return Result.failure(it) }
        validateBudget(budgetAmount).onFailure { return Result.failure(it) }
        validateNote(note).onFailure { return Result.failure(it) }
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
