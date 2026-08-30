package com.closeby.request.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.presentation.GroupedRequests

/**
 * Customer-facing screen: sections for Pending / Accepted / Rejected /
 * Completed / Cancelled requests.
 */
@Composable
fun CustomerRequestsScreen(
    grouped: GroupedRequests,
    modifier: Modifier = Modifier
) {
    val sections = listOf(
        "Pending" to grouped.pending,
        "Accepted" to grouped.accepted,
        "Rejected" to grouped.rejected,
        "Completed" to grouped.completed,
        "Cancelled" to grouped.cancelled
    )

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        sections.forEach { (label, requests) ->
            if (requests.isNotEmpty()) {
                item {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(requests, key = { it.id }) { request ->
                    CustomerRequestCard(
                        request = request,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerRequestCard(
    request: ServiceRequest,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(request.serviceTitle, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${request.requestedDate} · ${request.startTime}–${request.endTime} · ${request.duration}",
                style = MaterialTheme.typography.bodySmall
            )
            request.budgetAmount?.let { amount ->
                Text(
                    "Budget: ${request.budgetCurrency} $amount",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "Status: ${request.status}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
