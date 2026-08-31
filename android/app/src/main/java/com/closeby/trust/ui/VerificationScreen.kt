package com.closeby.trust.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.trust.domain.model.VerificationStatus
import com.closeby.trust.presentation.VerificationFormState

@Composable
fun VerificationScreen(
    form: VerificationFormState,
    onBusinessNameChange: (String) -> Unit,
    onContactPhoneChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDocumentUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSubmit = form.status in setOf(
        VerificationStatus.NOT_SUBMITTED,
        VerificationStatus.REJECTED
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Verification status: ${form.status.name.replace('_', ' ')}")
                form.latestSubmission?.adminNote?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Admin note: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        OutlinedTextField(
            value = form.businessName,
            onValueChange = onBusinessNameChange,
            label = { Text("Business name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit
        )
        OutlinedTextField(
            value = form.contactPhone,
            onValueChange = onContactPhoneChange,
            label = { Text("Contact phone") },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = canSubmit
        )
        OutlinedTextField(
            value = form.documentUrl,
            onValueChange = onDocumentUrlChange,
            label = { Text("Document URL (optional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit
        )

        if (canSubmit) {
            Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Submit for verification")
            }
        }
    }
}
