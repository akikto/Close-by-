package com.closeby.feature.provider.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.feature.provider.domain.model.ManagedServiceSummary
import com.closeby.feature.provider.domain.model.ProviderProfile
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProviderProfileScreen(
    profile: ProviderProfile,
    onMyServices: () -> Unit,
    onEditAvailability: () -> Unit,
    onServiceClick: (String) -> Unit,
    onProviderRequests: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (profile.profileImageUrl != null) {
                            AsyncImage(
                                model = profile.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = profile.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .padding(top = 12.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (profile.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(
                                "★ ${"%.1f".format(profile.rating)} (${profile.reviewCount} reviews)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            profile.distanceLabel?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            profile.phoneNumber?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (profile.isOwnProfile) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onMyServices, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                                Text("My Services")
                            }
                            OutlinedButton(onClick = onProviderRequests, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                                Text("Requests")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onEditAvailability, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Text("Edit Availability")
                        }
                    }
                }
            }
        }
        item {
            Text("Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (profile.services.isEmpty()) {
            item { Text("No active services.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(profile.services, key = { it.id }) { service ->
                ServiceSummaryCard(service = service, onClick = { onServiceClick(service.id) })
            }
        }
        item {
            Text("Availability", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(profile.availability, key = { it.dayOfWeek }) { entry ->
            AvailabilityRow(entry)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceSummaryCard(service: ManagedServiceSummary, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(service.title, style = MaterialTheme.typography.titleSmall)
            Text(
                "${service.category.displayName} · ${service.subcategory.displayName}",
                style = MaterialTheme.typography.bodySmall
            )
            service.price?.let { Text(it.formatted(), style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun AvailabilityRow(entry: ProviderAvailability) {
    val dayLabel = entry.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val timeLabel = if (entry.isAvailable && entry.startTime != null && entry.endTime != null) {
        "${entry.startTime} – ${entry.endTime}"
    } else {
        "Not available"
    }
    Text("$dayLabel: $timeLabel", style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun ProviderProfileLoading(modifier: Modifier = Modifier) {
    BoxCentered { CircularProgressIndicator() }
}

@Composable
fun ProviderProfileError(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    BoxCentered(modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun BoxCentered(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}
