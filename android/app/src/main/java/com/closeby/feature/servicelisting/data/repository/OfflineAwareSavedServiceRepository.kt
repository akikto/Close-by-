package com.closeby.feature.servicelisting.data.repository

import com.closeby.app.core.network.NetworkMonitor
import com.closeby.app.core.network.NetworkStatus
import com.closeby.feature.servicelisting.data.local.LocalSavedServiceRepository
import com.closeby.feature.servicelisting.data.local.SavedServiceSyncQueue
import com.closeby.feature.servicelisting.data.model.SavedServiceInsertDto
import com.closeby.feature.servicelisting.data.remote.SavedServiceRemoteDataSource
import com.closeby.feature.servicelisting.domain.model.SavedServiceEntry
import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Local-first saved services with optional remote sync when online and signed in.
 * Anonymous users can save/unsave offline; mutations queue for account sync.
 */
class OfflineAwareSavedServiceRepository(
    private val local: LocalSavedServiceRepository,
    private val syncQueue: SavedServiceSyncQueue,
    private val networkMonitor: NetworkMonitor,
    private val userIdProvider: suspend () -> String?,
    private val remote: SavedServiceRemoteDataSource = SavedServiceRemoteDataSource()
) : SavedServiceRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncStarted = false

    init {
        startSyncObserver()
    }

    override fun observeSavedServiceIds(): Flow<Set<String>> = local.observeSavedServiceIds()

    override suspend fun getSavedEntries(): List<SavedServiceEntry> =
        local.getSavedEntries()

    override suspend fun save(serviceId: String) {
        local.save(serviceId)
        val userId = userIdProvider()
        if (userId.isNullOrBlank()) {
            syncQueue.enqueueSave(serviceId)
            return
        }
        if (networkMonitor.status.value == NetworkStatus.ONLINE) {
            runCatching { remote.insert(SavedServiceInsertDto(userId = userId, serviceId = serviceId)) }
        } else {
            syncQueue.enqueueSave(serviceId)
        }
    }

    override suspend fun unsave(serviceId: String) {
        local.unsave(serviceId)
        val userId = userIdProvider()
        if (userId.isNullOrBlank()) {
            syncQueue.enqueueUnsave(serviceId)
            return
        }
        if (networkMonitor.status.value == NetworkStatus.ONLINE) {
            runCatching { remote.delete(userId, serviceId) }
        } else {
            syncQueue.enqueueUnsave(serviceId)
        }
    }

    override suspend fun isSaved(serviceId: String): Boolean = local.isSaved(serviceId)

    override suspend fun migrateLocalToAccount(userId: String, localIds: Set<String>) {
        localIds.forEach { id ->
            runCatching { remote.insert(SavedServiceInsertDto(userId = userId, serviceId = id)) }
        }
        syncQueue.clear()
        refreshFromRemote(userId)
    }

    suspend fun syncPending() {
        val userId = userIdProvider() ?: return
        if (networkMonitor.status.value != NetworkStatus.ONLINE) return
        val pending = syncQueue.drain()
        if (pending.isEmpty()) return
        pending.forEach { mutation ->
            when (mutation) {
                is SavedServiceSyncQueue.PendingMutation.Save ->
                    runCatching { remote.insert(SavedServiceInsertDto(userId, mutation.serviceId)) }
                is SavedServiceSyncQueue.PendingMutation.Unsave ->
                    runCatching { remote.delete(userId, mutation.serviceId) }
            }
        }
        syncQueue.clear()
        refreshFromRemote(userId)
    }

    private suspend fun refreshFromRemote(userId: String) {
        if (networkMonitor.status.value != NetworkStatus.ONLINE) return
        val remoteIds = runCatching { remote.getByUser(userId).map { it.serviceId }.toSet() }
            .getOrDefault(emptySet())
        remoteIds.forEach { local.save(it) }
    }

    private fun startSyncObserver() {
        if (syncStarted) return
        syncStarted = true
        scope.launch {
            networkMonitor.status.collect { status ->
                if (status == NetworkStatus.ONLINE) {
                    syncPending()
                }
            }
        }
    }
}
