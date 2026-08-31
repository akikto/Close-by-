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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.contact.data.AndroidContactLauncher
import com.closeby.request.presentation.RequestDetailsViewModel
import com.closeby.request.ui.RequestDetailsScreen
import com.closeby.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsRoute(
    requestId: String,
    providerId: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contactLauncher = remember { AndroidContactLauncher(context) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val viewModel: RequestDetailsViewModel = viewModel(
        factory = remember(requestId, providerId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RequestDetailsViewModel(
                        requestId = requestId,
                        customerId = null,
                        providerId = providerId,
                        repository = ProviderDependenciesFactory.serviceRequestRepository(context),
                        clientSessionStorage = ProviderDependenciesFactory.clientSessionStorage(context)
                    ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(requestId) { viewModel.load() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Request Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Success -> RequestDetailsScreen(
                    request = state.data,
                    isProviderView = providerId != null,
                    contactLauncher = contactLauncher,
                    snackbarHostState = snackbarHostState,
                    onCancel = viewModel::cancel,
                    onAccept = viewModel::accept,
                    onReject = viewModel::reject,
                    onComplete = viewModel::complete
                )
                is UiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                    TextButton(onClick = viewModel::load, modifier = Modifier.align(Alignment.Center)) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
