package com.closeby.advertisement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.advertisement.presentation.LocalOffersUiState
import com.closeby.advertisement.presentation.LocalOffersViewModel
import com.closeby.app.core.di.AdvertisementDependenciesFactory

@Composable
fun LocalOffersSection(
    modifier: Modifier = Modifier,
    viewModel: LocalOffersViewModel = viewModel(
        factory = rememberLocalOffersFactory()
    )
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val uiState = state) {
        is LocalOffersUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is LocalOffersUiState.LocationUnavailable -> Unit
        is LocalOffersUiState.Error -> Unit
        is LocalOffersUiState.Ready -> {
            if (uiState.offers.isEmpty()) return
            Column(modifier = modifier.fillMaxWidth()) {
                Text(
                    text = "Local Offers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.offers, key = { it.ad.id }) { offer ->
                        AdBannerCard(
                            ad = offer.ad,
                            distanceLabel = offer.distanceLabel,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberLocalOffersFactory(): androidx.lifecycle.ViewModelProvider.Factory {
    val context = LocalContext.current
    return remember(context) {
        AdvertisementDependenciesFactory.localOffersViewModelFactory(context)
    }
}
