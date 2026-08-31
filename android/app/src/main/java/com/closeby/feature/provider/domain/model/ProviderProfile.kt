package com.closeby.feature.provider.domain.model

import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.feature.servicelisting.domain.model.ServiceCategory

/**
 * Public-facing provider profile. Never exposes exact latitude/longitude —
 * use [distanceLabel] for relative distance only.
 */
data class ProviderProfile(
    val id: String,
    val name: String,
    val profileImageUrl: String?,
    val category: ServiceCategory,
    val isVerified: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val phoneNumber: String?,
    val distanceLabel: String?,
    val services: List<ManagedServiceSummary>,
    val availability: List<ProviderAvailability>,
    val isOwnProfile: Boolean = false
)

data class ProviderProfileUpdate(
    val name: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)
