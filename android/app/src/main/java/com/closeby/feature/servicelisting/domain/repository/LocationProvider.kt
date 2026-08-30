package com.closeby.feature.servicelisting.domain.repository

import com.closeby.feature.servicelisting.domain.model.DistanceInfo
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import kotlinx.coroutines.flow.Flow

/**
 * Integration contract with Agent 2's Location + Nearby module.
 *
 * IMPORTANT: This module does NOT implement this interface. Agent 2 owns:
 *   - device location acquisition
 *   - distance calculation
 *   - radius filtering
 *   - nearest-first sorting
 *
 * Agent 3 (this module) only calls through this contract to enrich listings
 * with distance data and to sort/filter by radius. A no-op/mock
 * implementation is provided for previews and tests so this module can be
 * built and tested in isolation before the real Location module lands.
 */
interface LocationProvider {

    /** Current status of device location (permission/enabled/etc). */
    fun observeLocationStatus(): Flow<com.closeby.feature.servicelisting.domain.model.LocationStatus>

    /**
     * Given a list of listings, returns the same listings enriched with
     * [DistanceInfo] computed by the Location module. Agent 3 does not
     * perform this calculation itself.
     */
    suspend fun attachDistances(listings: List<ServiceListing>): List<ServiceListing>

    /**
     * Returns listings already sorted nearest-first by the Location module.
     * Used to satisfy the default sort contract without a duplicate engine.
     */
    suspend fun sortNearestFirst(listings: List<ServiceListing>): List<ServiceListing>
}
