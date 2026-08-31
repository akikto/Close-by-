package com.closeby.feature.servicelisting.data.repository

import com.closeby.feature.servicelisting.data.model.SavedServiceInsertDto
import com.closeby.feature.servicelisting.data.remote.SavedServiceRemoteDataSource
import com.closeby.feature.servicelisting.domain.model.SavedServiceEntry
import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class SupabaseSavedServiceRepository(
    private val userIdProvider: suspend () -> String?,
    private val remote: SavedServiceRemoteDataSource = SavedServiceRemoteDataSource()
) : SavedServiceRepository {

    private val _ids = MutableStateFlow<Set<String>>(emptySet())

    override fun observeSavedServiceIds(): Flow<Set<String>> = _ids.asStateFlow()

    override suspend fun getSavedEntries(): List<SavedServiceEntry> {
        refresh()
        return _ids.value.map { SavedServiceEntry(it, 0L) }
    }

    override suspend fun save(serviceId: String) {
        val userId = userIdProvider() ?: throw IllegalStateException("Sign in to save services.")
        remote.insert(SavedServiceInsertDto(userId = userId, serviceId = serviceId))
        _ids.value = _ids.value + serviceId
    }

    override suspend fun unsave(serviceId: String) {
        val userId = userIdProvider() ?: return
        remote.delete(userId, serviceId)
        _ids.value = _ids.value - serviceId
    }

    override suspend fun isSaved(serviceId: String): Boolean {
        if (_ids.value.isEmpty()) refresh()
        return serviceId in _ids.value
    }

    override suspend fun migrateLocalToAccount(userId: String, localIds: Set<String>) {
        localIds.forEach { id ->
            runCatching { remote.insert(SavedServiceInsertDto(userId = userId, serviceId = id)) }
        }
        refresh()
    }

    private suspend fun refresh() {
        val userId = userIdProvider() ?: return
        val dtos = remote.getByUser(userId)
        _ids.value = dtos.map { it.serviceId }.toSet()
    }
}
