package com.closeby.app.domain.model

import com.closeby.feature.servicelisting.domain.model.ServiceCategory

/**
 * Minimal domain model for a service provider.
 * Uses the canonical servicelisting [ServiceCategory] taxonomy.
 */
data class Provider(
    val id: String,
    val name: String,
    val category: ServiceCategory,
    val phoneNumber: String,
    val latitude: Double,
    val longitude: Double
)
