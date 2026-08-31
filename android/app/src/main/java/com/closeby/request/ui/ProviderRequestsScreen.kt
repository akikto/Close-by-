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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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

private val TABS = listOf(
    "New Requests" to ServiceRequestStatus.PENDING,
    "Accepted" to ServiceRequestStatus.ACCEPTED,
    "Rejected" to ServiceRequestStatus.REJECTED,
    "Completed" to ServiceRequestStatus.COMPLETED
)

@Composable
fun ProviderRequestsScreen(
    requests: List<ServiceRequest>,
    onAccept: (requestId: String) -> Unit,
    onReject: (requestId: String) -> Unit,
    onComplete: (requestId: String) -> Unit,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

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

        val filterStatus = TABS[selectedTab].second
        val filtered = requests.filter { it.status == filterStatus }

        LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            items(filtered, key = { it.id }) { request ->
                ProviderRequestCard(
                    request = request,
                    onAccept = { onAccept(request.id) },
                    onReject = { onReject(request.id) },
                    onComplete = { onComplete(request.id) },
                    contactLauncher = contactLauncher,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ProviderRequestCard(
    request: ServiceRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(request.serviceTitle, style = MaterialTheme.typography.titleSmall)
            request.customerName?.let { name ->
                Text("Customer: $name", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${request.requestedDate} · ${request.startTime}–${request.endTime} · ${request.duration}",
                style = MaterialTheme.typography.bodySmall
            )
            request.budgetAmount?.let { amount ->
                Text(
                    "Budget: ${request.budgetCurrency} $amount" +
                        (request.budgetUnit?.let { " / $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            request.note?.let { note ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(note, style = MaterialTheme.typography.bodyMedium)
            }
            Text("Status: ${request.status}", style = MaterialTheme.typography.labelMedium)

            request.customerPhone?.let { customerPhone ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CallProviderButton(
                        phoneNumber = customerPhone,
                        contactLauncher = contactLauncher,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    SmsProviderButton(
                        phoneNumber = customerPhone,
                        contactLauncher = contactLauncher,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (request.status == ServiceRequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept")
                    }
                }
            }
            if (request.status == ServiceRequestStatus.ACCEPTED) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete")
                }
            }
        }
    }
}
