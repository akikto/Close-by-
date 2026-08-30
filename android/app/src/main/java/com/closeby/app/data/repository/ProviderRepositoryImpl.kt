package com.closeby.app.data.repository

import com.closeby.app.data.mapper.toDomain
import com.closeby.app.data.remote.ProviderRemoteDataSource
import com.closeby.app.domain.model.Provider
import com.closeby.app.domain.repository.ProviderRepository

/**
 * Repository implementation backed by Supabase/PostgreSQL.
 *
 * getNearbyProviders currently does a naive fetch-all + no filtering;
 * a real distance/radius query (PostGIS or client-side haversine) is
 * intentionally deferred to the nearby-search feature task.
 */
class ProviderRepositoryImpl(
    private val remoteDataSource: ProviderRemoteDataSource
) : ProviderRepository {

    override suspend fun getProviderById(id: String): Provider? {
        return remoteDataSource.getProviderById(id)?.toDomain()
    }

    override suspend fun getNearbyProviders(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Provider> {
        // TODO(nearby-feature): replace with a real radius query.
        return remoteDataSource.getAllProviders().map { it.toDomain() }
    }
}
