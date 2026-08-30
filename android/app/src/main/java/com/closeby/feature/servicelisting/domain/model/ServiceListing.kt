package com.closeby.feature.servicelisting.domain.model

/**
 * Core domain model for a single service listing (vehicle, labour, or
 * equipment) offered by a provider on Close by.
 *
 * Kept intentionally lean so it maps cleanly onto a future backend table.
 * `distanceInfo` is NOT computed here — it is supplied by the Location
 * module (Agent 2) and attached to this model at the repository layer.
 */
data class ServiceListing(
    val id: String,
    val providerId: String,
    val category: ServiceCategory,
    val subcategory: ServiceSubcategory,
    val title: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val latitude: Double,
    val longitude: Double,
    val availability: AvailabilityStatus,
    val price: PriceInfo,
    val contactNumber: String,
    val providerName: String,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val isVerifiedProvider: Boolean = false,
    val distanceInfo: DistanceInfo? = null
)

enum class AvailabilityStatus(val label: String) {
    AVAILABLE_NOW("Available Now"),
    AVAILABLE_SOON("Available Soon"),
    UNAVAILABLE("Currently Unavailable");
}
