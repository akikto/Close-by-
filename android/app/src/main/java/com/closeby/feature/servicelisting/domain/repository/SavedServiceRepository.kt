package com.closeby.feature.servicelisting.domain.repository

import com.closeby.feature.servicelisting.domain.model.RecentlyViewedEntry
import com.closeby.feature.servicelisting.domain.model.SavedServiceEntry
import kotlinx.coroutines.flow.Flow

interface SavedServiceRepository {
    fun observeSavedServiceIds(): Flow<Set<String>>
    suspend fun getSavedEntries(): List<SavedServiceEntry>
    suspend fun save(serviceId: String)
    suspend fun unsave(serviceId: String)
    suspend fun isSaved(serviceId: String): Boolean
    suspend fun migrateLocalToAccount(userId: String, localIds: Set<String>)
}

interface RecentlyViewedRepository {
    fun observeRecentlyViewed(): Flow<List<RecentlyViewedEntry>>
    suspend fun recordView(serviceId: String)
    suspend fun getRecentlyViewed(): List<RecentlyViewedEntry>
    suspend fun clear()
}
