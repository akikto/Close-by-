package com.closeby.app.data.location

import com.closeby.feature.nearby.model.Coordinates
import com.closeby.feature.nearby.util.DistanceCalculator
import com.closeby.feature.nearby.util.DistanceFormatter
import com.closeby.feature.servicelisting.domain.model.DistanceInfo
import com.closeby.feature.servicelisting.domain.model.LocationStatus
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.repository.LocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Adapter implementing the servicelisting [LocationProvider] contract using
 * the canonical nearby distance math and a shared [LocationSession].
 */
class ServicelistingLocationAdapter(
    private val session: LocationSession
) : LocationProvider {

    override fun start(scope: CoroutineScope) {
        session.bind(scope)
    }

    override fun observeLocationStatus(): Flow<LocationStatus> = session.status

    override fun observeDistanceRefresh(): Flow<Unit> =
        session.coordinates
            .filterNotNull()
            .map { }
            .distinctUntilChanged()

    override fun retryLocation() {
        session.retry()
    }

    override suspend fun attachDistances(listings: List<ServiceListing>): List<ServiceListing> {
        val user = session.coordinates.value ?: return listings.map { listing ->
            listing.copy(distanceInfo = DistanceInfo(distanceKm = null, status = session.status.value))
        }
        return listings.map { listing ->
            listing.copy(distanceInfo = computeDistanceInfo(user, listing, session.status.value))
        }
    }

    override suspend fun sortNearestFirst(listings: List<ServiceListing>): List<ServiceListing> =
        listings.sortedBy { it.distanceInfo?.distanceKm ?: Double.MAX_VALUE }

    private fun computeDistanceInfo(
        user: Coordinates,
        listing: ServiceListing,
        status: LocationStatus
    ): DistanceInfo {
        val serviceCoords = Coordinates(listing.latitude, listing.longitude)
        val meters = DistanceCalculator.distanceMeters(user, serviceCoords)
        val km = meters / 1000.0
        return DistanceInfo(distanceKm = km, status = status)
    }

    companion object {
        /** Formats distance for UI — never exposes coordinates. */
        fun formatDistanceMeters(meters: Double): String = DistanceFormatter.formatWithSuffix(meters)
    }
}
