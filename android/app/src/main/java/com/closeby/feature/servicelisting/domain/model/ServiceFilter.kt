package com.closeby.feature.servicelisting.domain.model

/**
 * Filter criteria for narrowing down service listings.
 * `distanceKm` here is a filter INPUT (radius chosen by the user) — the
 * actual distance math still happens in the Location module.
 */
data class ServiceFilter(
    val category: ServiceCategory? = null,
    val subcategory: ServiceSubcategory? = null,
    val radiusKm: RadiusOption? = null,
    val availability: AvailabilityFilter = AvailabilityFilter.ANY,
    val minRating: Double? = null,
    val maxPrice: Double? = null
) {
    val isEmpty: Boolean
        get() = category == null && subcategory == null && radiusKm == null &&
            availability == AvailabilityFilter.ANY && minRating == null && maxPrice == null
}

enum class RadiusOption(val km: Double, val label: String) {
    ONE_KM(1.0, "1 km"),
    FIVE_KM(5.0, "5 km"),
    TEN_KM(10.0, "10 km"),
    TWENTY_FIVE_KM(25.0, "25 km");

    companion object {
        fun custom(km: Double): Double = km
    }
}

enum class AvailabilityFilter(val label: String) {
    ANY("Any"),
    AVAILABLE_NOW("Available Now"),
    AVAILABLE_ON_DATE("Available on selected date")
}

enum class RatingFilter(val minRating: Double, val label: String) {
    FOUR_PLUS(4.0, "4★+"),
    THREE_PLUS(3.0, "3★+")
}
