package com.closeby.app.core.di

import android.content.Context
import com.closeby.app.BuildConfig
import com.closeby.app.core.session.AndroidClientSessionStorage
import com.closeby.app.core.session.ClientSessionStorage
import com.closeby.app.data.auth.MockAuthRepository
import com.closeby.app.data.auth.SupabaseAuthRepository
import com.closeby.app.data.storage.MockServiceImageUploader
import com.closeby.app.data.storage.ServiceImageUploader
import com.closeby.app.data.storage.SupabaseServiceImageUploader
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.availability.data.repository.MockAvailabilityRepository
import com.closeby.availability.data.repository.SupabaseAvailabilityRepository
import com.closeby.availability.domain.repository.AvailabilityRepository
import com.closeby.feature.provider.data.repository.MockProviderManagementRepository
import com.closeby.feature.provider.data.repository.SupabaseProviderManagementRepository
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.advertisement.data.storage.AdImageUploader
import com.closeby.advertisement.domain.repository.AdvertisementRepository
import com.closeby.request.data.mock.InMemoryServiceRequestRepository
import com.closeby.request.data.repository.SupabaseServiceRequestRepository
import com.closeby.request.domain.repository.ServiceRequestRepository

object ProviderDependenciesFactory {

    private val hasSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    fun authRepository(): AuthRepository =
        if (hasSupabase) SupabaseAuthRepository() else MockAuthRepository()

    fun availabilityRepository(): AvailabilityRepository =
        if (hasSupabase) SupabaseAvailabilityRepository() else MockAvailabilityRepository()

    fun providerManagementRepository(): ProviderManagementRepository =
        if (hasSupabase) {
            SupabaseProviderManagementRepository(availabilityRepository = availabilityRepository())
        } else {
            MockProviderManagementRepository()
        }

    fun clientSessionStorage(context: Context): ClientSessionStorage =
        AndroidClientSessionStorage(context.applicationContext)

    fun serviceRequestRepository(context: Context): ServiceRequestRepository =
        if (hasSupabase) {
            SupabaseServiceRequestRepository(
                serviceRepository = ServiceRepositoryFactory.create(),
                availabilityRepository = availabilityRepository(),
                clientSessionStorage = clientSessionStorage(context)
            )
        } else {
            InMemoryServiceRequestRepository()
        }

    fun imageUploader(context: Context): ServiceImageUploader =
        if (hasSupabase) SupabaseServiceImageUploader(context.applicationContext)
        else MockServiceImageUploader()

    fun advertisementRepository(): AdvertisementRepository =
        AdvertisementDependenciesFactory.advertisementRepository()

    fun adImageUploader(context: Context): AdImageUploader =
        AdvertisementDependenciesFactory.adImageUploader(context)
}
