package com.closeby.app.core.di

import android.content.Context
import com.closeby.app.BuildConfig
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.feature.servicelisting.data.local.LocalRecentlyViewedRepository
import com.closeby.feature.servicelisting.data.local.LocalSavedServiceRepository
import com.closeby.feature.servicelisting.data.repository.SupabaseSavedServiceRepository
import com.closeby.feature.servicelisting.domain.repository.RecentlyViewedRepository
import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository

object SavedDependenciesFactory {

    private val hasSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private var localSaved: LocalSavedServiceRepository? = null
    private var localHistory: LocalRecentlyViewedRepository? = null

    fun savedServiceRepository(context: Context, authRepository: AuthRepository): SavedServiceRepository {
        val local = localSaved ?: LocalSavedServiceRepository(context).also { localSaved = it }
        return if (hasSupabase) {
            SupabaseSavedServiceRepository(
                userIdProvider = { authRepository.getCurrentSession()?.userId },
                remote = com.closeby.feature.servicelisting.data.remote.SavedServiceRemoteDataSource()
            )
        } else {
            local
        }
    }

    fun localSavedRepository(context: Context): LocalSavedServiceRepository =
        localSaved ?: LocalSavedServiceRepository(context).also { localSaved = it }

    fun recentlyViewedRepository(context: Context): RecentlyViewedRepository =
        localHistory ?: LocalRecentlyViewedRepository(context).also { localHistory = it }
}
