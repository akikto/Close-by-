package com.closeby.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.closeby.app.core.network.NetworkStatus

@Composable
fun OfflineBanner(
    status: NetworkStatus,
    isShowingCachedData: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (status == NetworkStatus.ONLINE && !isShowingCachedData) return

    val message = when {
        status == NetworkStatus.OFFLINE && isShowingCachedData ->
            "Offline — showing saved listings"
        status == NetworkStatus.OFFLINE ->
            "You're offline"
        status == NetworkStatus.RECONNECTING ->
            "Reconnecting…"
        isShowingCachedData ->
            "Showing cached listings"
        else -> return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )
    }
}
