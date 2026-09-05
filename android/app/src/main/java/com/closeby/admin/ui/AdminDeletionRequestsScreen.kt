package com.closeby.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.closeby.admin.domain.model.AdminDeletionRequestSummary
import com.closeby.util.UiState
import java.text.DateFormat
import java.util.Date

@Composable
fun AdminDeletionRequestsScreen(
    uiState: UiState<List<AdminDeletionRequestSummary>>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
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
                    Text("No account deletion requests")
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.data, key = { it.id }) { request ->
                        DeletionRequestCard(
                            request = request,
                            onApprove = { onApprove(request.id) },
                            onReject = { onReject(request.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeletionRequestCard(
    request: AdminDeletionRequestSummary,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                request.displayName ?: "User ${request.userId.take(8)}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Status: ${request.status}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Requested: ${dateFormat.format(Date(request.requestedAt))}",
                style = MaterialTheme.typography.bodySmall
            )
            request.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    "Reason: $reason",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (request.status == "PENDING") {
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Approve & mark completed")
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reject request")
                }
            }
        }
    }
}
