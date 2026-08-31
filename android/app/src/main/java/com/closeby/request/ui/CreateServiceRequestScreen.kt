package com.closeby.request.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.request.domain.model.BudgetUnit
import com.closeby.request.presentation.CreateRequestFormState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateServiceRequestScreen(
    serviceTitle: String,
    providerName: String,
    formState: CreateRequestFormState,
    requiresContact: Boolean,
    onSendRequest: (
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        duration: String,
        budgetAmount: Double?,
        budgetUnit: BudgetUnit?,
        note: String?,
        customerName: String?,
        customerPhone: String?
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dateText by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var startText by remember { mutableStateOf("09:00") }
    var endText by remember { mutableStateOf("17:00") }
    var duration by remember { mutableStateOf("1 Day") }
    var budgetText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

    val validationMessage = (formState as? CreateRequestFormState.ValidationError)?.message
    val errorMessage = (formState as? CreateRequestFormState.Error)?.message
    val isSubmitting = formState is CreateRequestFormState.Submitting

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Request Service", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(serviceTitle, style = MaterialTheme.typography.titleMedium)
        Text("Provider: $providerName", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        validationMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            label = { Text("Date (YYYY-MM-DD) *") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = startText,
                onValueChange = { startText = it },
                label = { Text("Start *") },
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = endText,
                onValueChange = { endText = it },
                label = { Text("End *") },
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration *") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = budgetText,
            onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("My Budget (optional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        )

        if (requiresContact) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Your contact (for the provider)", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Your name *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("Your phone *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val date = runCatching {
                            LocalDate.parse(dateText.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
                        }.getOrNull() ?: return@Button
                        val start = runCatching { LocalTime.parse(startText.trim()) }.getOrNull() ?: return@Button
                        val end = runCatching { LocalTime.parse(endText.trim()) }.getOrNull() ?: return@Button
                        onSendRequest(
                            date,
                            start,
                            end,
                            duration,
                            budgetText.toDoubleOrNull(),
                            BudgetUnit.DAY,
                            note.ifBlank { null },
                            customerName.ifBlank { null },
                            customerPhone.ifBlank { null }
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Send Request")
                }
            }
        }
    }
}
