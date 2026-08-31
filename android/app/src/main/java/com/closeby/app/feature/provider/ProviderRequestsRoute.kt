package com.closeby.app.feature.provider

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.closeby.request.presentation.ProviderRequestsViewModel
import com.closeby.request.ui.ProviderRequestsScreen
import com.closeby.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderRequestsRoute(
    providerId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contactLauncher = remember { AndroidContactLauncher(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: ProviderRequestsViewModel = viewModel(
        factory = remember(providerId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProviderRequestsViewModel(
                        providerId = providerId,
                        repository = ProviderDependenciesFactory.serviceRequestRepository()
                    ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(providerId) { viewModel.loadRequests() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Provider Requests") },
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
                is UiState.Success -> ProviderRequestsScreen(
                    requests = state.data,
                    onAccept = viewModel::accept,
                    onReject = viewModel::reject,
                    onComplete = viewModel::complete,
                    contactLauncher = contactLauncher,
                    snackbarHostState = snackbarHostState
                )
                is UiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
