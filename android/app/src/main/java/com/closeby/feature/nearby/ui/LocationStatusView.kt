package com.closeby.feature.nearby.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Small status row for "acquiring location…" / "location ready" type states.
 * Intended for a header/toolbar area, not a full-screen state (see
 * [LocationErrorState] and [LocationPermissionView] for full-screen states).
 */
@Composable
fun LocationStatusView(
    isLoading: Boolean,
    statusText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp)
            )
        }
        Text(text = statusText, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview
@Composable
private fun LocationStatusViewPreview() {
    LocationStatusView(isLoading = true, statusText = "Finding your location…")
}
