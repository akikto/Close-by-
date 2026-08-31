package com.closeby.app.feature.request

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.app.core.di.ServiceRepositoryFactory
import com.closeby.app.core.network.NetworkMonitorHolder
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.request.presentation.CreateRequestFormState
import com.closeby.request.presentation.CreateServiceRequestViewModel
import com.closeby.request.ui.CreateServiceRequestScreen
import com.closeby.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceRequestRoute(
    serviceId: String,
    onBack: () -> Unit,
    onRequestCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var listing by remember { mutableStateOf<ServiceListing?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(serviceId) {
        withContext(Dispatchers.IO) {
            ServiceRepositoryFactory.create(context).getServiceById(serviceId)
                .onSuccess { listing = it }
                .onFailure { loadError = it.message ?: "Service not found." }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Request Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                loadError != null -> Text(
                    loadError!!,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium
                )
                listing == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> {
                    val service = listing!!
                    val viewModel: CreateServiceRequestViewModel = viewModel(
                        key = service.id,
                        factory = remember(service.id) {
                            object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                    CreateServiceRequestViewModel(
                                        serviceId = service.id,
                                        providerId = service.providerId,
                                        serviceTitle = service.title,
                                        providerName = service.providerName,
                                        providerPhone = service.contactNumber,
                                        customerId = null,
                                        repository = ProviderDependenciesFactory.serviceRequestRepository(context),
                                        serviceRepository = ServiceRepositoryFactory.create(context),
                                        availabilityRepository = ProviderDependenciesFactory.availabilityRepository(),
                                        clientSessionStorage = ProviderDependenciesFactory.clientSessionStorage(context),
                                        networkMonitor = NetworkMonitorHolder.get(context)
                                    ) as T
                            }
                        }
                    )
                    val formState by viewModel.formState.collectAsState()
                    val serviceLoad by viewModel.serviceLoadState.collectAsState()

                    LaunchedEffect(formState) {
                        if (formState is CreateRequestFormState.Submitted) {
                            onRequestCreated()
                        }
                    }

                    when (val load = serviceLoad) {
                        is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        is UiState.Error -> Text(load.message, Modifier.align(Alignment.Center))
                        is UiState.Success -> CreateServiceRequestScreen(
                            serviceTitle = service.title,
                            providerName = service.providerName,
                            formState = formState,
                            requiresContact = true,
                            onSendRequest = viewModel::sendRequest,
                            onCancel = onBack
                        )
                        else -> Unit
                    }
                }
            }
        }
    }
}
