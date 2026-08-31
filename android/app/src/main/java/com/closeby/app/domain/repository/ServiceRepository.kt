package com.closeby.app.domain.repository

import com.closeby.app.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.ServiceCategory

/**
 * Contract for reading service listings. Backed by Supabase/PostgreSQL
 * in the data layer. No implementation yet — base project only defines
 * the shape so feature work can build against a stable interface.
 */
interface ServiceRepository {
    suspend fun getListingsByCategory(category: ServiceCategory): List<ServiceListing>
    suspend fun getListingById(id: String): ServiceListing?
}
