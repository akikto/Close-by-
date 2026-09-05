package com.closeby.feature.provider.domain.model

import com.closeby.feature.servicelisting.domain.model.ServiceCategory

/** Data required when a user explicitly chooses to become a provider. */
data class ProviderOnboardingInput(
    val name: String,
    val category: ServiceCategory,
    val phoneNumber: String,
    val latitude: Double,
    val longitude: Double
)
