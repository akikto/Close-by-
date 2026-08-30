package com.closeby.feature.servicelisting.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Foundation-only contract for saving/bookmarking services for later.
 * This is intentionally minimal — full "Saved Services" screen/UI is out of
 * scope for this module and can be built on top of this contract later.
 */
interface SavedServiceRepository {
    fun observeSavedServiceIds(): Flow<Set<String>>
    suspend fun toggleSaved(serviceId: String)
    suspend fun isSaved(serviceId: String): Boolean
}
