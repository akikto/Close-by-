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
import com.closeby.admin.domain.model.AdminAdvertisementSummary
import com.closeby.util.UiState

@Composable
fun AdminAdvertisementsScreen(
    uiState: UiState<List<AdminAdvertisementSummary>>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
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
                    Text("No advertisements")
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.data, key = { it.id }) { ad ->
                        AdCard(
                            ad = ad,
                            onApprove = { onApprove(ad.id) },
                            onReject = { onReject(ad.id) },
                            onPause = { onPause(ad.id) },
                            onResume = { onResume(ad.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdCard(
    ad: AdminAdvertisementSummary,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(ad.title, style = MaterialTheme.typography.titleMedium)
            Text(ad.businessName, style = MaterialTheme.typography.bodySmall)
            Text("Status: ${ad.status}", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (ad.status) {
                    "PENDING" -> {
                        Button(onClick = onApprove, modifier = Modifier.weight(1f)) { Text("Approve") }
                        OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Reject") }
                    }
                    "APPROVED" -> {
                        OutlinedButton(onClick = onPause, modifier = Modifier.fillMaxWidth()) { Text("Pause") }
                    }
                    "PAUSED" -> {
                        Button(onClick = onResume, modifier = Modifier.fillMaxWidth()) { Text("Resume") }
                    }
                }
            }
        }
    }
}
