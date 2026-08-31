package com.closeby.app.data.repository

import com.closeby.app.data.mapper.ServiceListingMapper
import com.closeby.app.data.remote.ServiceRemoteDataSource
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Supabase-backed implementation of the servicelisting [ServiceRepository].
 * Caches the latest fetch in a [Flow] for reactive UI updates.
 */
class SupabaseServiceRepository(
    private val remoteDataSource: ServiceRemoteDataSource = ServiceRemoteDataSource()
) : ServiceRepository {

    private val cache = MutableStateFlow<List<ServiceListing>>(emptyList())

    override fun observeServices(): Flow<List<ServiceListing>> = cache.asStateFlow()

    override suspend fun fetchServices(): Result<List<ServiceListing>> = runCatching {
        val listings = ServiceListingMapper.toDomainList(remoteDataSource.getAllActiveServices())
        cache.value = listings
        listings
    }

    override suspend fun getServiceById(id: String): Result<ServiceListing> = runCatching {
        val dto = remoteDataSource.getServiceById(id)
            ?: throw NoSuchElementException("No listing found for id=$id")
        ServiceListingMapper.toDomainOrNull(dto)
            ?: throw IllegalStateException("Service listing $id has invalid data")
    }
}
