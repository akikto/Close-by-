package com.closeby.feature.servicelisting.domain.repository

import com.closeby.feature.servicelisting.domain.model.ServiceListing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Integration contract with Agent 2's Location + Nearby module.
 *
 * Agent 3 only calls through this contract to enrich listings with distance
 * data and to sort/filter by radius. A mock implementation is provided for
 * previews and tests.
 */
interface LocationProvider {

    /** Start observing device location. No-op for mock implementations. */
    fun start(scope: CoroutineScope) {}

    /** Current status of device location (permission/enabled/etc). */
    fun observeLocationStatus(): Flow<com.closeby.feature.servicelisting.domain.model.LocationStatus>

    /**
     * Emits when distances should be recomputed (e.g. user location changed).
     * Default no-op for mock implementations.
     */
    fun observeDistanceRefresh(): Flow<Unit> = emptyFlow()

    /** Ask the location source to retry after permission grant or GPS enable. */
    fun retryLocation() {}

    /**
     * Given a list of listings, returns the same listings enriched with
     * distance info computed by the Location module.
     */
    suspend fun attachDistances(listings: List<ServiceListing>): List<ServiceListing>

    /** Returns listings sorted nearest-first using attached distance data. */
    suspend fun sortNearestFirst(listings: List<ServiceListing>): List<ServiceListing>
}
