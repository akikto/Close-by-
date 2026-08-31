package com.closeby.app.feature.provider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.availability.presentation.AvailabilityViewModel
import com.closeby.availability.ui.AvailabilityEditorScreen
import com.closeby.availability.ui.defaultWeek
import com.closeby.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityEditorRoute(
    providerId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: AvailabilityViewModel = viewModel(
        factory = remember(providerId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AvailabilityViewModel(providerId, ProviderDependenciesFactory.availabilityRepository()) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(providerId) { viewModel.load() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Availability") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Success -> AvailabilityEditorScreen(
                    entries = state.data.ifEmpty { defaultWeek(providerId) },
                    isSaving = false,
                    errorMessage = null,
                    onSave = viewModel::save
                )
                is UiState.Error -> AvailabilityEditorScreen(
                    entries = defaultWeek(providerId),
                    isSaving = false,
                    errorMessage = state.message,
                    onSave = viewModel::save
                )
            }
        }
    }
}
