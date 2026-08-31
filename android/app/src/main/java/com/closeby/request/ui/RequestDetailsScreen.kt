package com.closeby.request.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.contact.domain.ContactLauncher
import com.closeby.contact.ui.CallProviderButton
import com.closeby.contact.ui.SmsProviderButton
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus

@Composable
fun RequestDetailsScreen(
    request: ServiceRequest,
    isProviderView: Boolean,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onLeaveReview: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(request.serviceTitle, style = MaterialTheme.typography.titleLarge)
            request.providerName?.let { Text("Provider: $it", style = MaterialTheme.typography.bodyMedium) }
            request.customerName?.let { Text("Customer: $it", style = MaterialTheme.typography.bodyMedium) }
            Spacer(modifier = Modifier.height(12.dp))
            Text("${request.requestedDate} · ${request.startTime}–${request.endTime}")
            Text("Duration: ${request.duration}")
            request.budgetAmount?.let { Text("Budget: ${request.budgetCurrency} $it") }
            request.note?.let { Text("Note: $it", style = MaterialTheme.typography.bodyMedium) }
            Text("Status: ${request.status}", style = MaterialTheme.typography.labelLarge)

            val phone = if (isProviderView) request.customerPhone else request.providerPhone
            phone?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CallProviderButton(it, contactLauncher, snackbarHostState, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    SmsProviderButton(it, contactLauncher, snackbarHostState, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (isProviderView) {
                when (request.status) {
                    ServiceRequestStatus.PENDING -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Reject") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = onAccept, modifier = Modifier.weight(1f)) { Text("Accept") }
                        }
                    }
                    ServiceRequestStatus.ACCEPTED -> {
                        Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("Complete") }
                    }
                    ServiceRequestStatus.COMPLETED -> {
                        Button(onClick = onLeaveReview, modifier = Modifier.fillMaxWidth()) { Text("Leave Review") }
                    }
                    else -> Unit
                }
            } else if (request.status == ServiceRequestStatus.PENDING) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel Request") }
            } else if (request.status == ServiceRequestStatus.COMPLETED) {
                Button(onClick = onLeaveReview, modifier = Modifier.fillMaxWidth()) { Text("Leave Review") }
            }
        }
    }
}
