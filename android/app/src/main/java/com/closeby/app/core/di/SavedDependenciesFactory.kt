package com.closeby.app.core.di

import android.content.Context
import com.closeby.app.BuildConfig
import com.closeby.app.core.network.NetworkMonitorHolder
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.feature.servicelisting.data.local.LocalRecentlyViewedRepository
import com.closeby.feature.servicelisting.data.local.LocalSavedServiceRepository
import com.closeby.feature.servicelisting.data.local.SavedServiceSyncQueue
import com.closeby.feature.servicelisting.data.repository.OfflineAwareSavedServiceRepository
import com.closeby.feature.servicelisting.domain.repository.RecentlyViewedRepository
import com.closeby.app.feature.saved.SavedServiceMigrationManager
import com.closeby.feature.servicelisting.presentation.viewmodel.SavedServiceToggleViewModel

object SavedDependenciesFactory {

    private val hasSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private var localSaved: LocalSavedServiceRepository? = null
    private var localHistory: LocalRecentlyViewedRepository? = null
    private var offlineSaved: OfflineAwareSavedServiceRepository? = null
    private var migrationManager: SavedServiceMigrationManager? = null

    fun savedServiceToggleViewModelFactory(
        context: Context,
        authRepository: AuthRepository
    ): SavedServiceToggleViewModel =
        SavedServiceToggleViewModel(savedServiceRepository(context, authRepository))

    fun migrationManager(context: Context, authRepository: AuthRepository): SavedServiceMigrationManager {
        migrationManager?.let { return it }
        return SavedServiceMigrationManager(
            localRepository = localSavedRepository(context),
            savedRepository = savedServiceRepository(context, authRepository)
        ).also { migrationManager = it }
    }

    fun savedServiceRepository(context: Context, authRepository: AuthRepository): SavedServiceRepository {
        val local = localSaved ?: LocalSavedServiceRepository(context).also { localSaved = it }
        return if (hasSupabase) {
            offlineSaved ?: OfflineAwareSavedServiceRepository(
                local = local,
                syncQueue = SavedServiceSyncQueue(context),
                networkMonitor = NetworkMonitorHolder.get(context),
                userIdProvider = { authRepository.getCurrentSession()?.userId }
            ).also { offlineSaved = it }
        } else {
            local
        }
    }

    fun localSavedRepository(context: Context): LocalSavedServiceRepository =
        localSaved ?: LocalSavedServiceRepository(context).also { localSaved = it }

    fun recentlyViewedRepository(context: Context): RecentlyViewedRepository =
        localHistory ?: LocalRecentlyViewedRepository(context).also { localHistory = it }
}
