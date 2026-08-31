package com.closeby.app.data.repository

import com.closeby.app.core.error.AppErrorMapper
import com.closeby.app.data.cache.ServiceListingDiskCache
import com.closeby.app.data.mapper.ServiceListingMapper
import com.closeby.app.data.remote.ServiceRemoteDataSource
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Supabase-backed repository with disk cache fallback for offline browsing.
 */
class OfflineAwareServiceRepository(
    private val diskCache: ServiceListingDiskCache,
    private val remoteDataSource: ServiceRemoteDataSource = ServiceRemoteDataSource()
) : ServiceRepository {

    private val cache = MutableStateFlow<List<ServiceListing>>(emptyList())
    private var lastFetchWasCached = false

    val isShowingCachedData: Boolean get() = lastFetchWasCached

    override fun observeServices(): Flow<List<ServiceListing>> = cache.asStateFlow()

    override suspend fun fetchServices(): Result<List<ServiceListing>> {
        return runCatching {
            val dtos = remoteDataSource.getAllActiveServices()
            diskCache.save(dtos)
            val listings = ServiceListingMapper.toDomainList(dtos)
            lastFetchWasCached = false
            cache.value = listings
            listings
        }.recoverCatching { error ->
            val cached = diskCache.load()
            if (cached != null && cached.services.isNotEmpty()) {
                val listings = ServiceListingMapper.toDomainList(cached.services)
                lastFetchWasCached = true
                cache.value = listings
                listings
            } else {
                throw error
            }
        }.mapCatching { it }
    }

    override suspend fun getServiceById(id: String): Result<ServiceListing> = runCatching {
        try {
            val dto = remoteDataSource.getServiceById(id)
                ?: throw NoSuchElementException("No listing found for id=$id")
            ServiceListingMapper.toDomainOrNull(dto)
                ?: throw IllegalStateException("Service listing $id has invalid data")
        } catch (e: Exception) {
            val cached = diskCache.load()?.services?.firstOrNull { it.id == id }
            if (cached != null) {
                ServiceListingMapper.toDomainOrNull(cached)
                    ?: throw IllegalStateException("Cached service $id has invalid data")
            } else {
                throw e
            }
        }
    }

    fun userFacingError(throwable: Throwable): String = AppErrorMapper.toUserMessage(throwable)
}
