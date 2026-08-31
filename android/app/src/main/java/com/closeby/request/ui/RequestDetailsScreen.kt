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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.app.core.ui.components.ConfirmDialog
import com.closeby.contact.domain.ContactLauncher
import com.closeby.contact.ui.CallProviderButton
import com.closeby.contact.ui.SmsProviderButton
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus

private enum class PendingAction { Cancel, Reject, Complete }

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
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }

    pendingAction?.let { action ->
        val (title, message, confirmLabel) = when (action) {
            PendingAction.Cancel -> Triple(
                "Cancel request?",
                "This will cancel your service request.",
                "Cancel request"
            )
            PendingAction.Reject -> Triple(
                "Reject request?",
                "The customer will be notified that you declined.",
                "Reject"
            )
            PendingAction.Complete -> Triple(
                "Mark as completed?",
                "Confirm that this service was completed.",
                "Complete"
            )
        }
        ConfirmDialog(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            onConfirm = {
                when (action) {
                    PendingAction.Cancel -> onCancel()
                    PendingAction.Reject -> onReject()
                    PendingAction.Complete -> onComplete()
                }
                pendingAction = null
            },
            onDismiss = { pendingAction = null }
        )
    }

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
                            OutlinedButton(
                                onClick = { pendingAction = PendingAction.Reject },
                                modifier = Modifier.weight(1f)
                            ) { Text("Reject") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = onAccept, modifier = Modifier.weight(1f)) { Text("Accept") }
                        }
                    }
                    ServiceRequestStatus.ACCEPTED -> {
                        Button(
                            onClick = { pendingAction = PendingAction.Complete },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Complete") }
                    }
                    ServiceRequestStatus.COMPLETED -> {
                        Button(onClick = onLeaveReview, modifier = Modifier.fillMaxWidth()) { Text("Leave Review") }
                    }
                    else -> Unit
                }
            } else if (request.status == ServiceRequestStatus.PENDING) {
                OutlinedButton(
                    onClick = { pendingAction = PendingAction.Cancel },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel Request") }
            } else if (request.status == ServiceRequestStatus.COMPLETED) {
                Button(onClick = onLeaveReview, modifier = Modifier.fillMaxWidth()) { Text("Leave Review") }
            }
        }
    }
}
