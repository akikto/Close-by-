package com.closeby.advertisement.domain.validation

import com.closeby.advertisement.domain.model.AdvertisementInput
import com.closeby.contact.domain.PhoneNumberValidator

sealed class AdValidationError(message: String) : Exception(message) {
    data object EmptyBusinessName : AdValidationError("Business name is required.")
    data object EmptyTitle : AdValidationError("Ad title is required.")
    data object MissingImage : AdValidationError("An image is required for the advertisement.")
    data object InvalidPhone : AdValidationError("Enter a valid contact number.")
    data object InvalidDateRange : AdValidationError("End date must be after the start date.")
    data object InvalidStartDate : AdValidationError("Start date cannot be in the past.")
    data object InvalidRadius : AdValidationError("Target radius must be greater than zero.")
    data object InvalidLocation : AdValidationError("Set a valid ad location.")
}

object AdValidator {

    private const val MIN_RADIUS_METERS = 100
    private const val MAX_RADIUS_METERS = 100_000

    fun validate(input: AdvertisementInput, now: Long = System.currentTimeMillis()): Result<Unit> {
        if (input.businessName.isBlank()) {
            return Result.failure(AdValidationError.EmptyBusinessName)
        }
        if (input.title.isBlank()) {
            return Result.failure(AdValidationError.EmptyTitle)
        }
        if (input.imageUrl.isNullOrBlank()) {
            return Result.failure(AdValidationError.MissingImage)
        }
        if (!PhoneNumberValidator.isValid(input.contactNumber)) {
            return Result.failure(AdValidationError.InvalidPhone)
        }
        if (input.endAt <= input.startAt) {
            return Result.failure(AdValidationError.InvalidDateRange)
        }
        if (input.startAt < now) {
            return Result.failure(AdValidationError.InvalidStartDate)
        }
        if (input.targetRadiusMeters < MIN_RADIUS_METERS || input.targetRadiusMeters > MAX_RADIUS_METERS) {
            return Result.failure(AdValidationError.InvalidRadius)
        }
        if (!isValidLatitude(input.latitude) || !isValidLongitude(input.longitude)) {
            return Result.failure(AdValidationError.InvalidLocation)
        }
        return Result.success(Unit)
    }

    private fun isValidLatitude(value: Double): Boolean =
        value.isFinite() && value in -90.0..90.0

    private fun isValidLongitude(value: Double): Boolean =
        value.isFinite() && value in -180.0..180.0
}
