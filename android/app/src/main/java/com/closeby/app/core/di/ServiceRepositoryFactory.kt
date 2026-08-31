package com.closeby.app.core.di

import android.content.Context
import com.closeby.app.BuildConfig
import com.closeby.app.data.cache.ServiceListingDiskCache
import com.closeby.app.data.repository.OfflineAwareServiceRepository
import com.closeby.feature.servicelisting.data.mock.MockServiceRepository
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository

/**
 * Composition root for [ServiceRepository].
 * Uses Supabase when credentials are configured; otherwise falls back to mock data
 * so the app remains runnable without a backend.
 */
object ServiceRepositoryFactory {

    private var offlineAware: OfflineAwareServiceRepository? = null

    fun create(): ServiceRepository {
        val hasSupabaseConfig = BuildConfig.SUPABASE_URL.isNotBlank() &&
            BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

        return if (hasSupabaseConfig) {
            offlineAware ?: MockServiceRepository()
        } else {
            MockServiceRepository()
        }
    }

    fun create(context: Context): ServiceRepository {
        val hasSupabaseConfig = BuildConfig.SUPABASE_URL.isNotBlank() &&
            BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

        return if (hasSupabaseConfig) {
            offlineAware ?: OfflineAwareServiceRepository(
                diskCache = ServiceListingDiskCache(context.applicationContext)
            ).also { offlineAware = it }
        } else {
            MockServiceRepository()
        }
    }

    fun offlineAwareOrNull(): OfflineAwareServiceRepository? = offlineAware
}
