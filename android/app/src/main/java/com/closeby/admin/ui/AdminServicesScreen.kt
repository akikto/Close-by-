package com.closeby.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.admin.domain.model.AdminServiceSummary
import com.closeby.util.UiState

@Composable
fun AdminServicesScreen(
    uiState: UiState<List<AdminServiceSummary>>,
    onToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is UiState.Idle, is UiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            if (uiState.data.isEmpty()) {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No services")
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.data, key = { it.id }) { service ->
                        ServiceCard(
                            service = service,
                            onToggle = { enable -> onToggle(service.id, enable) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: AdminServiceSummary,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(service.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "${service.category} • ${service.providerName ?: service.providerId}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                when {
                    service.isDeleted -> "Deleted"
                    service.isActive -> "Active"
                    else -> "Disabled"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (service.isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
            if (!service.isDeleted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (service.isActive) {
                        OutlinedButton(
                            onClick = { onToggle(false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Disable")
                        }
                    } else {
                        Button(
                            onClick = { onToggle(true) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
        }
    }
}
