package com.closeby.app.feature.nearby

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.closeby.app.core.di.NearbyDependenciesFactory
import com.closeby.app.core.permissions.rememberLocationPermissionState
import com.closeby.feature.nearby.ui.LocationErrorState
import com.closeby.feature.nearby.ui.LocationErrorReason
import com.closeby.feature.nearby.ui.LocationPermissionView
import com.closeby.feature.servicelisting.domain.model.LocationStatus
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.presentation.screens.ServiceListingScreen
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceListingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Canonical nearby-services host: permission → location → Supabase listings → distance.
 * Shared by Home and Explore — do not duplicate this logic elsewhere.
 */
@Composable
fun NearbyServicesHost(
    onServiceClick: (ServiceListing) -> Unit,
    modifier: Modifier = Modifier,
    showFullFilters: Boolean = true,
    maxListings: Int? = null,
    showSearchBar: Boolean = true,
    viewModel: ServiceListingViewModel = viewModel(
        factory = rememberNearbyListingFactory()
    )
) {
    val locationStatus by viewModel.locationStatus.collectAsState()
    val permissionState = rememberLocationPermissionState { granted ->
        if (granted) viewModel.retryLocation()
    }

    LaunchedEffect(permissionState.isGranted) {
        if (permissionState.isGranted) {
            viewModel.retryLocation()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !permissionState.isGranted -> {
                LocationPermissionView(
                    permanentlyDenied = permissionState.shouldShowRationale,
                    onRequestPermission = permissionState.requestPermission,
                    onOpenSettings = permissionState.openAppSettings,
                    modifier = Modifier.fillMaxSize()
                )
            }
            locationStatus == LocationStatus.PERMISSION_DENIED -> {
                LocationPermissionView(
                    permanentlyDenied = false,
                    onRequestPermission = permissionState.requestPermission,
                    onOpenSettings = permissionState.openAppSettings,
                    modifier = Modifier.fillMaxSize()
                )
            }
            locationStatus == LocationStatus.LOCATION_DISABLED -> {
                LocationErrorState(
                    reason = LocationErrorReason.GPS_DISABLED,
                    onRetry = viewModel::retryLocation,
                    modifier = Modifier.fillMaxSize()
                )
            }
            locationStatus == LocationStatus.UNAVAILABLE -> {
                LocationErrorState(
                    reason = LocationErrorReason.LOCATION_UNAVAILABLE,
                    onRetry = viewModel::retryLocation,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                ServiceListingScreen(
                    viewModel = viewModel,
                    onServiceClick = onServiceClick,
                    modifier = Modifier.fillMaxSize(),
                    showFullFilters = showFullFilters,
                    maxListings = maxListings,
                    showSearchBar = showSearchBar
                )
            }
        }
    }
}

@Composable
fun rememberNearbyListingFactory(): androidx.lifecycle.ViewModelProvider.Factory {
    val context = LocalContext.current
    return remember(context) {
        val stack = NearbyDependenciesFactory.createStack(context)
        NearbyDependenciesFactory.listingViewModelFactory(stack)
    }
}

@Composable
fun rememberNearbyStack(): NearbyDependenciesFactory.NearbyStack {
    val context = LocalContext.current
    return remember(context) {
        NearbyDependenciesFactory.createStack(context)
    }
}
