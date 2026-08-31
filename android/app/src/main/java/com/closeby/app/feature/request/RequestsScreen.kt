package com.closeby.app.feature.request

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.closeby.request.presentation.CustomerRequestsViewModel
import com.closeby.request.ui.CustomerRequestsScreen
import com.closeby.util.UiState

@Composable
fun RequestsScreen(
    onOpenRequestDetails: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val contactLauncher = remember { AndroidContactLauncher(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: CustomerRequestsViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CustomerRequestsViewModel(
                        customerId = null,
                        repository = ProviderDependenciesFactory.serviceRequestRepository(context),
                        clientSessionStorage = ProviderDependenciesFactory.clientSessionStorage(context)
                    ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRequests() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                is UiState.Success -> {
                    val allEmpty = state.data.pending.isEmpty() && state.data.accepted.isEmpty() &&
                        state.data.rejected.isEmpty() && state.data.completed.isEmpty() &&
                        state.data.cancelled.isEmpty()
                    if (allEmpty) {
                        Text(
                            "No requests yet. Browse services and tap Request Service.",
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        CustomerRequestsScreen(
                            grouped = state.data,
                            contactLauncher = contactLauncher,
                            snackbarHostState = snackbarHostState,
                            onCancel = viewModel::cancel,
                            onOpenDetails = onOpenRequestDetails
                        )
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    TextButton(
                        onClick = viewModel::loadRequests,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
