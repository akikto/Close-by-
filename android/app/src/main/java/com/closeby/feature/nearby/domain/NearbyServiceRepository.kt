package com.closeby.feature.nearby.domain

import com.closeby.feature.nearby.model.NearbySearchFilters
import com.closeby.feature.nearby.model.ServiceLocation

/**
 * INTEGRATION POINT.
 *
 * This module does NOT implement data fetching. The base project (Supabase-backed)
 * should provide a concrete implementation of this interface, e.g.:
 *
 *   class SupabaseNearbyServiceRepository(
 *       private val supabase: SupabaseClient
 *   ) : NearbyServiceRepository {
 *       override suspend fun findCandidateLocations(
 *           filters: NearbySearchFilters
 *       ): List<ServiceLocation> {
 *           // query the services table, apply category/query/availability/rating
 *           // filters, and map rows to ServiceLocation(serviceId, lat, lon)
 *       }
 *   }
 *
 * Deliberately coarse-grained (filter, not radius/location) — geographic filtering and
 * sorting is this module's job and happens in [GetNearbyServicesUseCase], not here. This
 * keeps the interface stable regardless of whether the data source does its own
 * geo-filtering (e.g. PostGIS) as an optimization later.
 */
interface NearbyServiceRepository {
    suspend fun findCandidateLocations(filters: NearbySearchFilters): List<ServiceLocation>
}
