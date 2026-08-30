package com.closeby.feature.nearby.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.closeby.feature.nearby.util.DistanceFormatter

/**
 * Small pill/label showing a relative distance, e.g. "2.4 km away".
 *
 * PRIVACY: only ever pass a computed distance in — never raw coordinates.
 */
@Composable
fun DistanceLabel(
    distanceMeters: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = DistanceFormatter.formatWithSuffix(distanceMeters),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview
@Composable
private fun DistanceLabelPreview() {
    DistanceLabel(distanceMeters = 2400.0)
}
