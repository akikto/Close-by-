package com.closeby.app.feature.advertisement

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.advertisement.presentation.MyAdvertisementsViewModel
import com.closeby.advertisement.ui.MyAdvertisementsScreen
import com.closeby.app.core.di.AdvertisementDependenciesFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAdvertisementsRoute(
    ownerId: String,
    onBack: () -> Unit,
    onCreateAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MyAdvertisementsViewModel = viewModel(
        factory = remember(ownerId) {
            AdvertisementDependenciesFactory.myAdsViewModelFactory(context, ownerId)
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("My Advertisements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateAd) {
                Text("+")
            }
        }
    ) { padding ->
        MyAdvertisementsScreen(
            state = uiState,
            onPause = viewModel::pauseAd,
            statusLabel = viewModel::statusLabel,
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}
