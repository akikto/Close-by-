package com.closeby.app.domain.model

/**
 * Minimal placeholder domain model for a service provider.
 * Intentionally not over-engineered — fields will expand once the
 * provider feature is implemented.
 */
data class Provider(
    val id: String,
    val name: String,
    val category: ServiceCategory,
    val phoneNumber: String,
    val latitude: Double,
    val longitude: Double
)
