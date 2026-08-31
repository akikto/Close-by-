package com.closeby.app.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.closeby.advertisement.data.mock.InMemoryAdvertisementRepository
import com.closeby.advertisement.data.repository.SupabaseAdvertisementRepository
import com.closeby.advertisement.data.storage.AdImageUploader
import com.closeby.advertisement.data.storage.MockAdImageUploader
import com.closeby.advertisement.data.storage.SupabaseAdImageUploader
import com.closeby.advertisement.domain.repository.AdvertisementRepository
import com.closeby.advertisement.presentation.CreateAdvertisementViewModel
import com.closeby.advertisement.presentation.LocalOffersViewModel
import com.closeby.advertisement.presentation.MyAdvertisementsViewModel
import com.closeby.app.BuildConfig

object AdvertisementDependenciesFactory {

    private val hasSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    fun advertisementRepository(): AdvertisementRepository =
        if (hasSupabase) SupabaseAdvertisementRepository() else InMemoryAdvertisementRepository()

    fun adImageUploader(context: Context): AdImageUploader =
        if (hasSupabase) SupabaseAdImageUploader(context.applicationContext)
        else MockAdImageUploader()

    fun createAdViewModelFactory(context: Context, ownerId: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CreateAdvertisementViewModel(
                    ownerId = ownerId,
                    repository = advertisementRepository(),
                    imageUploader = adImageUploader(context)
                ) as T
        }

    fun myAdsViewModelFactory(context: Context, ownerId: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MyAdvertisementsViewModel(
                    ownerId = ownerId,
                    repository = advertisementRepository()
                ) as T
        }

    fun localOffersViewModelFactory(context: Context): ViewModelProvider.Factory {
        val stack = NearbyDependenciesFactory.createStack(context)
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LocalOffersViewModel(
                    repository = advertisementRepository(),
                    locationSession = stack.locationSession
                ) as T
        }
    }
}
