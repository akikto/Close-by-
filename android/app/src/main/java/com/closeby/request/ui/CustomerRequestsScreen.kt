package com.closeby.request.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.contact.domain.ContactLauncher
import com.closeby.contact.ui.CallProviderButton
import com.closeby.contact.ui.SmsProviderButton
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.presentation.GroupedRequests

private val TABS = listOf(
    "Pending" to ServiceRequestStatus.PENDING,
    "Accepted" to ServiceRequestStatus.ACCEPTED,
    "Rejected" to ServiceRequestStatus.REJECTED,
    "Cancelled" to ServiceRequestStatus.CANCELLED,
    "Completed" to ServiceRequestStatus.COMPLETED
)

@Composable
fun CustomerRequestsScreen(
    grouped: GroupedRequests,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    onCancel: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val all = listOf(
        grouped.pending,
        grouped.accepted,
        grouped.rejected,
        grouped.cancelled,
        grouped.completed
    )
    val filtered = all.getOrElse(selectedTab) { emptyList() }

    Column(modifier = modifier.fillMaxWidth()) {
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            TABS.forEachIndexed { index, (label, _) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            items(filtered, key = { it.id }) { request ->
                CustomerRequestCard(
                    request = request,
                    contactLauncher = contactLauncher,
                    snackbarHostState = snackbarHostState,
                    onCancel = { onCancel(request.id) },
                    onOpenDetails = { onOpenDetails(request.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "No requests in this tab.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerRequestCard(
    request: ServiceRequest,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    onCancel: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(request.serviceTitle, style = MaterialTheme.typography.titleSmall)
            request.providerName?.let {
                Text("Provider: $it", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${request.requestedDate} · ${request.startTime}–${request.endTime} · ${request.duration}",
                style = MaterialTheme.typography.bodySmall
            )
            request.budgetAmount?.let { amount ->
                Text("Budget: ${request.budgetCurrency} $amount", style = MaterialTheme.typography.bodySmall)
            }
            request.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text("Status: ${request.status}", style = MaterialTheme.typography.labelMedium)

            request.providerPhone?.let { phone ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CallProviderButton(
                        phoneNumber = phone,
                        contactLauncher = contactLauncher,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    SmsProviderButton(
                        phoneNumber = phone,
                        contactLauncher = contactLauncher,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row {
                TextButton(onClick = onOpenDetails) { Text("Details") }
                if (request.status == ServiceRequestStatus.PENDING) {
                    OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(12.dp)) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
