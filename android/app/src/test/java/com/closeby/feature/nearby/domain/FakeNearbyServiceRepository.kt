package com.closeby.feature.nearby.domain

import com.closeby.feature.nearby.model.NearbySearchFilters
import com.closeby.feature.nearby.model.ServiceLocation

/** Deterministic in-memory fake for [NearbyServiceRepository], used only in tests. */
class FakeNearbyServiceRepository(
    private val locations: List<ServiceLocation> = emptyList(),
    private val failure: Throwable? = null
) : NearbyServiceRepository {
    override suspend fun findCandidateLocations(filters: NearbySearchFilters): List<ServiceLocation> {
        failure?.let { throw it }
        return locations
    }
}
