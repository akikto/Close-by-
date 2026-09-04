package com.closeby.app.feature.request

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.ui.components.CloseByLogo
import com.closeby.request.data.mock.InMemoryServiceRequestRepository
import com.closeby.request.presentation.CustomerRequestsViewModel
import com.closeby.request.ui.CustomerRequestsScreen
import com.closeby.util.UiState

/**
 * Requests screen — hosts Agent 5's customer-facing Requests feature
 * (Pending / Accepted / Rejected / Completed / Cancelled). Wired here with
 * an in-memory demo repository so the tab is runnable end to end.
 *
 * TODO: swap [InMemoryServiceRequestRepository] for a Supabase-backed
 * `ServiceRequestRepository` that enforces authorization server-side (see
 * Agent 5's INTEGRATION_NOTES.md). TODO: pass the real signed-in
 * `customerId` once provider/customer accounts exist (anonymous/null is
 * used for now, per the "no forced login" requirement).
 */
private class CustomerRequestsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CustomerRequestsViewModel(
            customerId = null,
            repository = InMemoryServiceRequestRepository()
        ) as T
}

@Composable
fun RequestsScreen() {
    val viewModel: CustomerRequestsViewModel =
        viewModel(factory = remember { CustomerRequestsViewModelFactory() })
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRequests() }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CloseByLogo(size = 56.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator()
                }
                is UiState.Success -> CustomerRequestsScreen(grouped = state.data)
                is UiState.Error -> Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
