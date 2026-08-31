package com.closeby.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.util.UiState

@Composable
fun AdminProvidersScreen(
    uiState: UiState<List<AdminProviderSummary>>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSuspend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Search providers") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        when (uiState) {
            is UiState.Idle, is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                if (uiState.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No providers found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.data, key = { it.id }) { provider ->
                            ProviderCard(provider = provider, onSuspend = { onSuspend(provider.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: AdminProviderSummary,
    onSuspend: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(provider.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Verification: ${provider.verificationStatus.name}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Rating: ${provider.rating} • Active: ${provider.isActive}",
                style = MaterialTheme.typography.bodySmall
            )
            if (!provider.isSuspended) {
                OutlinedButton(
                    onClick = onSuspend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Suspend")
                }
            } else {
                Text(
                    "Suspended",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
