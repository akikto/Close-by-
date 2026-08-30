package com.closeby.feature.nearby.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Shown when a nearby search succeeds but returns zero results within the radius. */
@Composable
fun NearbyEmptyState(
    radiusLabel: String,
    onIncreaseRadius: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Nothing nearby yet")
        Text(text = "No services found within $radiusLabel. Try a wider radius.")
        if (onIncreaseRadius != null) {
            OutlinedButton(onClick = onIncreaseRadius) {
                Text("Increase search radius")
            }
        }
    }
}

@Preview
@Composable
private fun NearbyEmptyStatePreview() {
    NearbyEmptyState(radiusLabel = "5 km", onIncreaseRadius = {})
}
