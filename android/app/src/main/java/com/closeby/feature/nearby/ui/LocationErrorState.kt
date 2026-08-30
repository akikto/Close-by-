package com.closeby.feature.nearby.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** The reason a full-screen [LocationErrorState] is being shown. */
enum class LocationErrorReason {
    GPS_DISABLED,
    LOCATION_UNAVAILABLE,
    NETWORK_UNAVAILABLE,
    UNKNOWN_ERROR
}

/**
 * Full-screen error/retry state for location or nearby-search failures that are
 * NOT a permission problem (see [LocationPermissionView] for that case).
 */
@Composable
fun LocationErrorState(
    reason: LocationErrorReason,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = titleFor(reason))
        Text(text = detail ?: messageFor(reason))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun titleFor(reason: LocationErrorReason): String = when (reason) {
    LocationErrorReason.GPS_DISABLED -> "Location services are off"
    LocationErrorReason.LOCATION_UNAVAILABLE -> "Can't get your location"
    LocationErrorReason.NETWORK_UNAVAILABLE -> "You're offline"
    LocationErrorReason.UNKNOWN_ERROR -> "Something went wrong"
}

private fun messageFor(reason: LocationErrorReason): String = when (reason) {
    LocationErrorReason.GPS_DISABLED -> "Turn on location services to find nearby services."
    LocationErrorReason.LOCATION_UNAVAILABLE -> "We couldn't determine your location. Try again."
    LocationErrorReason.NETWORK_UNAVAILABLE -> "Check your connection and try again."
    LocationErrorReason.UNKNOWN_ERROR -> "Please try again."
}

@Preview
@Composable
private fun LocationErrorStatePreview() {
    LocationErrorState(reason = LocationErrorReason.GPS_DISABLED, onRetry = {})
}
