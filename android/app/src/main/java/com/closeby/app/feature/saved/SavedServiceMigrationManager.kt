package com.closeby.app.feature.saved

import com.closeby.feature.servicelisting.data.local.LocalSavedServiceRepository
import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class MigrationPromptState {
    data object Hidden : MigrationPromptState()
    data class Prompt(val localCount: Int, val userId: String) : MigrationPromptState()
    data object Migrating : MigrationPromptState()
    data class Success(val migratedCount: Int) : MigrationPromptState()
    data class Error(val message: String, val userId: String, val localIds: Set<String>) : MigrationPromptState()
}

/**
 * Coordinates anonymous → account saved-service migration with explicit user consent.
 */
class SavedServiceMigrationManager(
    private val localRepository: LocalSavedServiceRepository,
    private val savedRepository: SavedServiceRepository
) {
    private val _state = MutableStateFlow<MigrationPromptState>(MigrationPromptState.Hidden)
    val state: StateFlow<MigrationPromptState> = _state.asStateFlow()

    fun onSignedIn(userId: String) {
        val localIds = localRepository.currentIds()
        if (localIds.isEmpty()) {
            _state.value = MigrationPromptState.Hidden
            return
        }
        _state.value = MigrationPromptState.Prompt(localIds.size, userId)
    }

    fun dismiss() {
        _state.value = MigrationPromptState.Hidden
    }

    suspend fun migrate(userId: String, localIds: Set<String>) {
        _state.value = MigrationPromptState.Migrating
        val result = runCatching { savedRepository.migrateLocalToAccount(userId, localIds) }
        if (result.isSuccess) {
            _state.value = MigrationPromptState.Success(localIds.size)
        } else {
            _state.value = MigrationPromptState.Error(
                message = result.exceptionOrNull()?.message
                    ?: "Could not sync saved services. Your local saves are preserved.",
                userId = userId,
                localIds = localIds
            )
        }
    }

    suspend fun retry(userId: String, localIds: Set<String>) = migrate(userId, localIds)

    fun clearSuccess() {
        _state.value = MigrationPromptState.Hidden
    }
}
