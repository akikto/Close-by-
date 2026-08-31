package com.closeby.app.core.di

import com.closeby.app.BuildConfig
import com.closeby.app.data.repository.SupabaseServiceRepository
import com.closeby.feature.servicelisting.data.mock.MockServiceRepository
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository

/**
 * Composition root for [ServiceRepository].
 * Uses Supabase when credentials are configured; otherwise falls back to mock data
 * so the app remains runnable without a backend.
 */
object ServiceRepositoryFactory {

    fun create(): ServiceRepository {
        val hasSupabaseConfig = BuildConfig.SUPABASE_URL.isNotBlank() &&
            BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

        return if (hasSupabaseConfig) {
            SupabaseServiceRepository()
        } else {
            MockServiceRepository()
        }
    }
}
