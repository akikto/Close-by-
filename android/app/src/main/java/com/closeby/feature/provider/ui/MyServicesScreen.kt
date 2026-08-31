package com.closeby.feature.provider.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.closeby.feature.provider.domain.model.ManagedService

@Composable
fun MyServicesScreen(
    services: List<ManagedService>,
    onAddService: () -> Unit,
    onEdit: (String) -> Unit,
    onView: (String) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = services.filter { it.isActive }
    val disabled = services.filter { !it.isActive }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = onAddService, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Add Service")
            }
        }
        if (active.isNotEmpty()) {
            item { SectionTitle("Active services") }
            items(active, key = { it.id }) { service ->
                ManagedServiceCard(
                    service = service,
                    onEdit = { onEdit(service.id) },
                    onView = { onView(service.id) },
                    onToggleActive = { onToggleActive(service.id, false) },
                    onDelete = { onDelete(service.id) },
                    toggleLabel = "Disable"
                )
            }
        }
        if (disabled.isNotEmpty()) {
            item { SectionTitle("Disabled services") }
            items(disabled, key = { it.id }) { service ->
                ManagedServiceCard(
                    service = service,
                    onEdit = { onEdit(service.id) },
                    onView = { onView(service.id) },
                    onToggleActive = { onToggleActive(service.id, true) },
                    onDelete = { onDelete(service.id) },
                    toggleLabel = "Enable"
                )
            }
        }
        if (services.isEmpty()) {
            item {
                Text("No services yet. Tap Add Service to create your first listing.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun ManagedServiceCard(
    service: ManagedService,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    toggleLabel: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(service.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "${service.category.displayName} · ${service.subcategory.displayName}",
                style = MaterialTheme.typography.bodySmall
            )
            service.price?.let { Text(it.formatted(), style = MaterialTheme.typography.bodyMedium) }
            Text(service.availability.label, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("Edit")
                }
                OutlinedButton(onClick = onView, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("View")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onToggleActive, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text(toggleLabel)
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("Delete")
                }
            }
        }
    }
}
