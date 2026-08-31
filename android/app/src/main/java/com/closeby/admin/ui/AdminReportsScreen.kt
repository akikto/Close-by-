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
import com.closeby.trust.domain.model.Report
import com.closeby.trust.domain.model.ReportStatus
import com.closeby.util.UiState

@Composable
fun AdminReportsScreen(
    uiState: UiState<List<Report>>,
    onResolve: (String) -> Unit,
    onDismiss: (String) -> Unit,
    onReview: (String) -> Unit,
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
                    Text("No reports")
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.data, key = { it.id }) { report ->
                        ReportCard(
                            report = report,
                            onResolve = { onResolve(report.id) },
                            onDismiss = { onDismiss(report.id) },
                            onReview = { onReview(report.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: Report,
    onResolve: () -> Unit,
    onDismiss: () -> Unit,
    onReview: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${report.targetType.name} report", style = MaterialTheme.typography.titleMedium)
            Text("Reason: ${report.reason.name}", style = MaterialTheme.typography.bodySmall)
            Text("Status: ${report.status.name}", style = MaterialTheme.typography.bodySmall)
            report.description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
            if (report.status == ReportStatus.OPEN || report.status == ReportStatus.UNDER_REVIEW) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (report.status == ReportStatus.OPEN) {
                        OutlinedButton(onClick = onReview, modifier = Modifier.weight(1f)) {
                            Text("Review")
                        }
                    }
                    Button(onClick = onResolve, modifier = Modifier.weight(1f)) {
                        Text("Resolve")
                    }
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}
