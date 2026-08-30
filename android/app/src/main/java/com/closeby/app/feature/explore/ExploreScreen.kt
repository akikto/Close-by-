package com.closeby.app.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.closeby.feature.servicelisting.data.mock.MockServiceRepository
import com.closeby.feature.servicelisting.data.mock.MockLocationProvider
import com.closeby.feature.servicelisting.presentation.screens.ServiceListingScreen
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceListingViewModel

/**
 * Explore screen — hosts Agent 3's Service Listing feature (search, filter,
 * sort). Wired here with the module's own demo/mock repository +
 * location provider so the tab is runnable end to end.
 *
 * TODO: swap [MockServiceRepository] for a Supabase-backed `ServiceRepository`
 * and [MockLocationProvider] for an adapter over Agent 2's
 * `com.closeby.feature.nearby` location module (see that module's README).
 * TODO: navigate to a Service Details route on [onServiceClick] once that
 * route is added to `CloseByNavHost`.
 */
private class ServiceListingViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ServiceListingViewModel(
            serviceRepository = MockServiceRepository(),
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
