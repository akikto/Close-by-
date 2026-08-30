package com.closeby.feature.servicelisting.domain.repository

import com.closeby.feature.servicelisting.domain.model.ServiceListing
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for service listings.
 *
 * This module (Agent 3) depends ONLY on this interface, never on a concrete
 * data source. The Base Project (Agent 1) or a data module is expected to
 * provide the real implementation (e.g. backed by Supabase). A mock
 * implementation is provided under `data/mock` for previews and tests.
 */
interface ServiceRepository {

    /** Stream of all service listings visible to the current user/area. */
    fun observeServices(): Flow<List<ServiceListing>>

    /** One-shot fetch, useful for pull-to-refresh / retry actions. */
    suspend fun fetchServices(): Result<List<ServiceListing>>

    /** Fetch a single listing by id for the details screen. */
    suspend fun getServiceById(id: String): Result<ServiceListing>
}
