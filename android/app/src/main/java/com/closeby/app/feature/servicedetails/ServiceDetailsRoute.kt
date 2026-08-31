package com.closeby.app.feature.servicedetails

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.NearbyDependenciesFactory
import com.closeby.contact.data.AndroidContactLauncher
import com.closeby.feature.servicelisting.presentation.screens.ServiceDetailsScreen
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceDetailsActions
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceDetailsViewModel

@Composable
fun ServiceDetailsRoute(
    serviceId: String,
    onBack: () -> Unit,
    onViewProviderProfile: (String) -> Unit = {},
    onRequestService: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stack = remember(context) { NearbyDependenciesFactory.createStack(context) }
    val viewModel: ServiceDetailsViewModel = viewModel(
        factory = NearbyDependenciesFactory.detailsViewModelFactory(stack, serviceId)
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val contactLauncher = remember { AndroidContactLauncher(context) }

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        ServiceDetailsScreen(
            viewModel = viewModel,
            actions = object : ServiceDetailsActions {
                override fun onCallProvider(phoneNumber: String) {
                    contactLauncher.call(phoneNumber).onFailure { }
                }

                override fun onSmsProvider(phoneNumber: String) {
                    contactLauncher.sms(phoneNumber).onFailure { }
                }

                override fun onViewProviderProfile(providerId: String) {
                    onViewProviderProfile(providerId)
                }

                override fun onRequestService(
                    serviceId: String,
                    providerId: String,
                    serviceTitle: String,
                    providerName: String,
                    providerPhone: String
                ) {
                    onRequestService(serviceId)
                }

                override fun onBack() = onBack()
            },
            modifier = Modifier.padding(padding)
        )
    }
}
