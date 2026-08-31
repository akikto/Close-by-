package com.closeby.feature.provider.domain.model

import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory

/** Provider-owned service listing including management metadata. */
data class ManagedService(
    val id: String,
    val providerId: String,
    val category: ServiceCategory,
    val subcategory: ServiceSubcategory,
    val title: String,
    val description: String,
    val imageUrls: List<String>,
    val latitude: Double,
    val longitude: Double,
    val availability: AvailabilityStatus,
    val price: PriceInfo?,
    val contactNumber: String,
    val isActive: Boolean,
    val isDeleted: Boolean,
    val rating: Double = 0.0,
    val reviewCount: Int = 0
)

data class ManagedServiceSummary(
    val id: String,
    val title: String,
    val category: ServiceCategory,
    val subcategory: ServiceSubcategory,
    val price: PriceInfo?,
    val availability: AvailabilityStatus,
    val isActive: Boolean
)

data class ServiceFormInput(
    val category: ServiceCategory,
    val subcategory: ServiceSubcategory,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val availability: AvailabilityStatus,
    val contactNumber: String,
    val imageUrls: List<String> = emptyList(),
    val priceAmount: Double? = null,
    val priceUnit: com.closeby.feature.servicelisting.domain.model.PriceUnit? = null,
    val priceIsStarting: Boolean = false
)
