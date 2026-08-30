package com.closeby.feature.nearby.domain

import com.closeby.feature.nearby.util.DistanceCalculator

/**
 * Reusable use case: given the user's location, a radius, and optional filters,
 * return matching services sorted nearest-first.
 *
 * Deliberately depends only on [NearbyServiceRepository] (an interface) — no
 * Supabase / Android / database coupling here, so it can be unit tested in plain
 * JVM tests and later wired to the real data layer by the base project.
 */
class GetNearbyServicesUseCase(
    private val repository: NearbyServiceRepository
) {
    suspend operator fun invoke(params: NearbySearchParams): Result<List<NearbyServiceResult>> {
        return try {
            val candidates = repository.findCandidateLocations(params.filters)

            val withinRadius = candidates.mapNotNull { candidate ->
                val distanceMeters = DistanceCalculator.distanceMeters(
                    from = params.userLocation,
                    to = candidate.toCoordinates()
                )
                val distanceKm = distanceMeters / 1000.0
                if (distanceKm <= params.radiusKm) {
                    NearbyServiceResult(serviceId = candidate.serviceId, distanceMeters = distanceMeters)
                } else {
                    null
                }
            }

            Result.success(withinRadius.sortedBy { it.distanceMeters })
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
