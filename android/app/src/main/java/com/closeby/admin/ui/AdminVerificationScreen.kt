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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.admin.domain.model.AdminProviderSummary
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.util.UiState

@Composable
fun AdminVerificationScreen(
    uiState: UiState<List<AdminProviderSummary>>,
    selectedTab: VerificationStatus,
    onTabSelected: (VerificationStatus) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onSuspend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        VerificationStatus.PENDING,
        VerificationStatus.APPROVED,
        VerificationStatus.REJECTED,
        VerificationStatus.SUSPENDED
    )

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
            tabs.forEach { status ->
                Tab(
                    selected = selectedTab == status,
                    onClick = { onTabSelected(status) },
                    text = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

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
                        Text("No providers in this category")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.data, key = { it.id }) { provider ->
                            VerificationProviderCard(
                                provider = provider,
                                onApprove = { onApprove(provider.id) },
                                onReject = { onReject(provider.id) },
                                onSuspend = { onSuspend(provider.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationProviderCard(
    provider: AdminProviderSummary,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(provider.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Status: ${provider.verificationStatus.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Rating: ${provider.rating} (${provider.reviewCount} reviews)",
                style = MaterialTheme.typography.bodySmall
            )
            if (provider.verificationStatus == VerificationStatus.PENDING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1f)) {
                        Text("Approve")
                    }
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                        Text("Reject")
                    }
                }
            }
            if (!provider.isSuspended && provider.verificationStatus != VerificationStatus.SUSPENDED) {
                OutlinedButton(
                    onClick = onSuspend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Suspend Provider")
                }
            }
        }
    }
}
