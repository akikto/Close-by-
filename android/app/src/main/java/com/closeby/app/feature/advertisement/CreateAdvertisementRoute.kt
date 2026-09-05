package com.closeby.app.feature.advertisement

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.advertisement.presentation.CreateAdUiState
import com.closeby.advertisement.presentation.CreateAdvertisementViewModel
import com.closeby.advertisement.ui.CreateAdvertisementScreen
import com.closeby.app.core.location.DeviceCoordinatesReader
import com.closeby.app.core.di.AdvertisementDependenciesFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdvertisementRoute(
    ownerId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: CreateAdvertisementViewModel = viewModel(
        factory = remember(ownerId) {
            AdvertisementDependenciesFactory.createAdViewModelFactory(context, ownerId)
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val coords = DeviceCoordinatesReader.readCurrent(context)
        viewModel.load(
            initialLatitude = coords?.latitude,
            initialLongitude = coords?.longitude
        )
    }
    LaunchedEffect(uiState) {
        if (uiState is CreateAdUiState.Saved) onCreated()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Create Advertisement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        CreateAdvertisementScreen(
            state = uiState,
            onSave = viewModel::save,
            onUpdate = viewModel::updateForm,
            onPickImage = viewModel::queueImage,
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}
