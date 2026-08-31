package com.closeby.app.domain.model

import com.closeby.feature.servicelisting.domain.model.ServiceCategory

/**
 * Minimal placeholder domain model for a listing (a vehicle, a labour
 * offering, or a piece of equipment) posted by a provider.
 */
data class ServiceListing(
    val id: String,
    val providerId: String,
    val title: String,
    val category: ServiceCategory,
    val description: String
)
