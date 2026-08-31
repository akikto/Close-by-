package com.closeby.trust.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.trust.domain.model.ReportReason
import com.closeby.trust.presentation.ReportFormState

@Composable
fun ReportScreen(
    form: ReportFormState,
    onReasonSelected: (ReportReason) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Report ${form.targetType.name.lowercase()}", style = MaterialTheme.typography.titleLarge)
        Text("Select a reason", style = MaterialTheme.typography.titleSmall)

        ReportReason.entries.forEach { reason ->
            FilterChip(
                selected = form.reason == reason,
                onClick = { onReasonSelected(reason) },
                label = { Text(reason.name.replace('_', ' ')) }
            )
        }

        OutlinedTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            label = { Text("Additional details (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            enabled = form.reason != null
        ) {
            Text("Submit report")
        }
    }
}
