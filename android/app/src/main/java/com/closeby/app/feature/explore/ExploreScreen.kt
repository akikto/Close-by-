package com.closeby.app.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.closeby.app.core.di.ServiceRepositoryFactory
import com.closeby.feature.servicelisting.data.mock.MockLocationProvider
import com.closeby.feature.servicelisting.presentation.screens.ServiceListingScreen
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceListingViewModel

/**
 * Explore screen — hosts Agent 3's Service Listing feature (search, filter,
 * sort). Uses Supabase when configured, otherwise mock data for local dev.
 *
 * TODO: replace [MockLocationProvider] with an adapter over Agent 2's
 * `com.closeby.feature.nearby` location module (Phase 2).
 * TODO: navigate to a Service Details route on [onServiceClick].
 */
private class ServiceListingViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ServiceListingViewModel(
            serviceRepository = ServiceRepositoryFactory.create(),
            locationProvider = MockLocationProvider()
        ) as T
}

@Composable
fun ExploreScreen() {
    val viewModel: ServiceListingViewModel = viewModel(factory = remember { ServiceListingViewModelFactory() })
    ServiceListingScreen(
        viewModel = viewModel,
        onServiceClick = { /* TODO: navigate to Service Details once wired into CloseByNavHost */ }
    )
}
