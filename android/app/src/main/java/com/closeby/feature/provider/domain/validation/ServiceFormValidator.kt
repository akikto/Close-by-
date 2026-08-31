package com.closeby.feature.provider.domain.validation

import com.closeby.contact.domain.PhoneNumberValidator
import com.closeby.feature.provider.domain.model.ServiceFormInput
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory

sealed class ServiceFormValidationError(message: String) : Exception(message) {
    data object EmptyTitle : ServiceFormValidationError("Service title is required.")
    data object EmptyDescription : ServiceFormValidationError("Description is required.")
    data object InvalidCategory : ServiceFormValidationError("Please select a valid category.")
    data object InvalidSubcategory : ServiceFormValidationError("Please select a valid subcategory.")
    data object SubcategoryMismatch :
        ServiceFormValidationError("Subcategory does not match the selected category.")
    data object InvalidPrice : ServiceFormValidationError("Price must be a positive number.")
    data object MissingPriceUnit :
        ServiceFormValidationError("Select a price unit when entering a price.")
    data object InvalidPhone : ServiceFormValidationError("Enter a valid contact number.")
    data object InvalidLocation : ServiceFormValidationError("Set a valid service location.")
}

object ServiceFormValidator {

    fun validate(input: ServiceFormInput): Result<Unit> {
        if (input.title.isBlank()) {
            return Result.failure(ServiceFormValidationError.EmptyTitle)
        }
        if (input.description.isBlank()) {
            return Result.failure(ServiceFormValidationError.EmptyDescription)
        }
        if (input.category !in ServiceCategory.entries) {
            return Result.failure(ServiceFormValidationError.InvalidCategory)
        }
        if (input.subcategory !in ServiceSubcategory.entries) {
            return Result.failure(ServiceFormValidationError.InvalidSubcategory)
        }
        if (input.subcategory.parent != input.category) {
            return Result.failure(ServiceFormValidationError.SubcategoryMismatch)
        }
        input.priceAmount?.let { amount ->
            if (amount <= 0.0) {
                return Result.failure(ServiceFormValidationError.InvalidPrice)
            }
            if (input.priceUnit == null || input.priceUnit == PriceUnit.NONE) {
                return Result.failure(ServiceFormValidationError.MissingPriceUnit)
            }
        }
        if (!PhoneNumberValidator.isValid(input.contactNumber)) {
            return Result.failure(ServiceFormValidationError.InvalidPhone)
        }
        if (!isValidLatitude(input.latitude) || !isValidLongitude(input.longitude)) {
            return Result.failure(ServiceFormValidationError.InvalidLocation)
        }
        return Result.success(Unit)
    }

    private fun isValidLatitude(value: Double): Boolean =
        value.isFinite() && value in -90.0..90.0

    private fun isValidLongitude(value: Double): Boolean =
        value.isFinite() && value in -180.0..180.0
}
