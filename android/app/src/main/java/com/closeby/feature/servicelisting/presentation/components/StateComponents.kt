package com.closeby.feature.servicelisting.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.closeby.feature.servicelisting.presentation.model.EmptyReason

@Composable
fun ServiceListLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Loading services" },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = "Finding services near you...",
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ServiceListEmptyState(reason: EmptyReason, onClearFilters: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val (title, subtitle) = when (reason) {
        EmptyReason.NO_SEARCH_RESULTS -> "No matching services" to "Try a different search term."
        EmptyReason.NO_SERVICES_IN_RADIUS -> "Nothing nearby yet" to "Try increasing your search radius."
        EmptyReason.NO_SERVICES_IN_CATEGORY -> "No services in this category" to "Try another category or clear filters."
        EmptyReason.NO_SERVICES_AVAILABLE -> "No services available" to "Check back soon, new listings are added often."
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .semantics { contentDescription = "$title. $subtitle" },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (onClearFilters != null && reason != EmptyReason.NO_SERVICES_AVAILABLE) {
            Button(onClick = onClearFilters, modifier = Modifier.padding(top = 16.dp)) {
                Text("Clear filters")
            }
        }
    }
}

@Composable
fun ServiceListErrorState(
    message: String,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .semantics {
                contentDescription = "Error: $message"
                liveRegion = LiveRegionMode.Assertive
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Something went wrong", style = MaterialTheme.typography.titleMedium)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (isRetryable) {
            Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                Text("Retry")
            }
        }
    }
}
