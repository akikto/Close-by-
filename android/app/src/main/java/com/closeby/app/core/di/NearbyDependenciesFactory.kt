package com.closeby.app.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.closeby.app.core.network.NetworkMonitor
import com.closeby.app.core.network.NetworkMonitorHolder
import com.closeby.app.core.di.TrustDependenciesFactory
import com.closeby.app.data.location.LocationSession
import com.closeby.app.data.location.ServicelistingLocationAdapter
import com.closeby.feature.nearby.location.AndroidLocationProvider
import com.closeby.feature.servicelisting.domain.repository.LocationProvider
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceDetailsViewModel
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceListingViewModel

/**
 * Composition root for nearby-search dependencies shared by Home and Explore.
 */
object NearbyDependenciesFactory {

    data class NearbyStack(
        val serviceRepository: ServiceRepository,
        val locationProvider: LocationProvider,
        val locationSession: LocationSession,
        val blockedProviderIdsProvider: suspend () -> Set<String>,
        val networkMonitor: NetworkMonitor
    )

    fun createStack(context: Context): NearbyStack {
        val deviceLocation = AndroidLocationProvider(context.applicationContext)
        val session = LocationSession(deviceLocation)
        val locationAdapter = ServicelistingLocationAdapter(session)
        val trustRepository = TrustDependenciesFactory.trustRepository(context)
        val authRepository = ProviderDependenciesFactory.authRepository()
        return NearbyStack(
            serviceRepository = ServiceRepositoryFactory.create(context),
            locationProvider = locationAdapter,
            locationSession = session,
            blockedProviderIdsProvider = {
                val userId = authRepository.getCurrentSession()?.userId ?: return@NearbyStack emptySet()
                trustRepository.getBlockedProviderIds(userId).getOrDefault(emptySet())
            },
            networkMonitor = NetworkMonitorHolder.get(context)
        )
    }

    fun listingViewModelFactory(stack: NearbyStack): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                ServiceListingViewModel(
                    serviceRepository = stack.serviceRepository,
                    locationProvider = stack.locationProvider,
                    blockedProviderIdsProvider = stack.blockedProviderIdsProvider,
                    networkMonitor = stack.networkMonitor
                ) as T
        }

    fun detailsViewModelFactory(stack: NearbyStack, serviceId: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                ServiceDetailsViewModel(
                    serviceId = serviceId,
                    serviceRepository = stack.serviceRepository,
                    locationProvider = stack.locationProvider
                ) as T
        }
}
