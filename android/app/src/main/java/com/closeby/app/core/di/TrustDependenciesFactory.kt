package com.closeby.app.core.di

import android.content.Context
import com.closeby.app.BuildConfig
import com.closeby.trust.data.repository.MockTrustRepository
import com.closeby.trust.data.repository.SupabaseTrustRepository
import com.closeby.trust.domain.repository.TrustRepository

object TrustDependenciesFactory {

    private val hasSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    fun trustRepository(context: Context): TrustRepository =
        if (hasSupabase) {
            SupabaseTrustRepository(
                serviceRequestRepository = ProviderDependenciesFactory.serviceRequestRepository(context)
            )
        } else {
            MockTrustRepository(
                serviceRequestRepository = ProviderDependenciesFactory.serviceRequestRepository(context)
            )
        }
}
