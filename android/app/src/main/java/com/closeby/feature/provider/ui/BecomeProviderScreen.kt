package com.closeby.feature.provider.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.feature.provider.presentation.BecomeProviderUiState
import com.closeby.feature.servicelisting.domain.model.ServiceCategory

@Composable
fun BecomeProviderScreen(
    state: BecomeProviderUiState,
    onUpdate: ((BecomeProviderUiState.Ready) -> BecomeProviderUiState.Ready) -> Unit,
    onCaptureLocation: () -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (state) {
        is BecomeProviderUiState.Ready -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Become a Provider",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "List your vehicles, labour, or equipment. This is separate from your customer account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { value -> onUpdate { it.copy(name = value) } },
                    label = { Text("Business / display name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { value -> onUpdate { it.copy(phoneNumber = value) } },
                    label = { Text("Contact phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Primary category", style = MaterialTheme.typography.titleSmall)
                ServiceCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onUpdate { it.copy(category = category) } },
                        label = { Text(category.displayName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedButton(
                    onClick = onCaptureLocation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.locationCaptured) "Location captured — tap to refresh"
                        else "Use current GPS location"
                    )
                }
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.locationCaptured
                ) {
                    Text("Create provider profile")
                }
            }
        }
        BecomeProviderUiState.Saving -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is BecomeProviderUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismissError) { Text("Back to form") }
            }
        }
        is BecomeProviderUiState.Success -> Unit
        BecomeProviderUiState.Idle -> Unit
    }
}
