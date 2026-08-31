package com.closeby.app.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
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
        val locationSession: LocationSession
    )

    fun createStack(context: Context): NearbyStack {
        val deviceLocation = AndroidLocationProvider(context.applicationContext)
        val session = LocationSession(deviceLocation)
        val locationAdapter = ServicelistingLocationAdapter(session)
        return NearbyStack(
            serviceRepository = ServiceRepositoryFactory.create(),
            locationProvider = locationAdapter,
            locationSession = session
        )
    }

    fun listingViewModelFactory(stack: NearbyStack): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                ServiceListingViewModel(
                    serviceRepository = stack.serviceRepository,
                    locationProvider = stack.locationProvider
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
