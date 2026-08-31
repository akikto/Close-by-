package com.closeby.advertisement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.closeby.advertisement.domain.model.AdStatus
import com.closeby.advertisement.domain.model.Advertisement
import com.closeby.advertisement.presentation.MyAdsUiState

@Composable
fun MyAdvertisementsScreen(
    state: MyAdsUiState,
    onPause: (String) -> Unit,
    statusLabel: (AdStatus) -> String,
    modifier: Modifier = Modifier
) {
    when (state) {
        is MyAdsUiState.Loading -> {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is MyAdsUiState.Error -> {
            Column(
                modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is MyAdsUiState.Ready -> {
            if (state.ads.isEmpty()) {
                Column(
                    modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No advertisements yet.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.ads, key = { it.id }) { ad ->
                        MyAdRow(
                            ad = ad,
                            statusLabel = statusLabel(ad.status),
                            onPause = { onPause(ad.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyAdRow(
    ad: Advertisement,
    statusLabel: String,
    onPause: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AdBannerCard(ad = ad, distanceLabel = null)
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        ad.rejectionReason?.let { reason ->
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (ad.status == AdStatus.APPROVED) {
            OutlinedButton(
                onClick = onPause,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Pause")
            }
        }
    }
}
