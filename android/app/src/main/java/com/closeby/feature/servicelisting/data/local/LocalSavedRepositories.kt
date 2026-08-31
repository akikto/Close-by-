package com.closeby.feature.servicelisting.data.local

import android.content.Context
import com.closeby.feature.servicelisting.domain.model.RecentlyViewedEntry
import com.closeby.feature.servicelisting.domain.model.SavedServiceEntry
import com.closeby.feature.servicelisting.domain.repository.RecentlyViewedRepository
import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_SAVED = "closeby_saved_services"
private const val PREFS_HISTORY = "closeby_recently_viewed"
private const val MAX_HISTORY = 20

class LocalSavedServiceRepository(context: Context) : SavedServiceRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_SAVED, Context.MODE_PRIVATE)
    private val _ids = MutableStateFlow(readIds())

    override fun observeSavedServiceIds(): Flow<Set<String>> = _ids.asStateFlow()

    override suspend fun getSavedEntries(): List<SavedServiceEntry> =
        _ids.value.map { SavedServiceEntry(it, 0L) }

    override suspend fun save(serviceId: String) {
        val updated = _ids.value + serviceId
        _ids.value = updated
        persist(updated)
    }

    override suspend fun unsave(serviceId: String) {
        val updated = _ids.value - serviceId
        _ids.value = updated
        persist(updated)
    }

    override suspend fun isSaved(serviceId: String): Boolean = serviceId in _ids.value

    override suspend fun migrateLocalToAccount(userId: String, localIds: Set<String>) = Unit

    fun currentIds(): Set<String> = _ids.value

    private fun readIds(): Set<String> =
        prefs.getStringSet("ids", emptySet()).orEmpty()

    private fun persist(ids: Set<String>) {
        prefs.edit().putStringSet("ids", ids).apply()
    }
}

class LocalRecentlyViewedRepository(context: Context) : RecentlyViewedRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE)
    private val _entries = MutableStateFlow(readEntries())

    override fun observeRecentlyViewed(): Flow<List<RecentlyViewedEntry>> = _entries.asStateFlow()

    override suspend fun recordView(serviceId: String) {
        val now = System.currentTimeMillis()
        val existing = _entries.value.filter { it.serviceId != serviceId }
        val updated = listOf(RecentlyViewedEntry(serviceId, now)) + existing
        _entries.value = updated.take(MAX_HISTORY)
        persist(_entries.value)
    }

    override suspend fun getRecentlyViewed(): List<RecentlyViewedEntry> = _entries.value

    override suspend fun clear() {
        _entries.value = emptyList()
        prefs.edit().remove("entries").apply()
    }

    private fun readEntries(): List<RecentlyViewedEntry> =
        prefs.getString("entries", null)
            ?.split("|")
            ?.mapNotNull { raw ->
                val parts = raw.split(":")
                if (parts.size == 2) {
                    RecentlyViewedEntry(parts[0], parts[1].toLongOrNull() ?: 0L)
                } else null
            }.orEmpty()

    private fun persist(entries: List<RecentlyViewedEntry>) {
        val serialized = entries.joinToString("|") { "${it.serviceId}:${it.viewedAt}" }
        prefs.edit().putString("entries", serialized).apply()
    }
}
