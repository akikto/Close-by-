package com.closeby.app.feature.provider

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.app.core.location.DeviceCoordinatesReader
import com.closeby.feature.provider.presentation.BecomeProviderUiState
import com.closeby.feature.provider.presentation.BecomeProviderViewModel
import com.closeby.feature.provider.ui.BecomeProviderScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BecomeProviderRoute(
    onBack: () -> Unit,
    onProviderCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: BecomeProviderViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BecomeProviderViewModel(
                        authRepository = ProviderDependenciesFactory.authRepository(),
                        providerRepository = ProviderDependenciesFactory.providerManagementRepository()
                    ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is BecomeProviderUiState.Success) {
            onProviderCreated((uiState as BecomeProviderUiState.Success).providerId)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Become a Provider") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        BecomeProviderScreen(
            state = uiState,
            onUpdate = viewModel::updateForm,
            onCaptureLocation = {
                scope.launch {
                    val coords = DeviceCoordinatesReader.readCurrent(context)
                    if (coords != null) {
                        viewModel.setLocation(coords.latitude, coords.longitude)
                    } else {
                        viewModel.setError(
                            "Could not get GPS location. Enable location services and try again."
                        )
                    }
                }
            },
            onSubmit = viewModel::submit,
            onDismissError = viewModel::resetErrorToReady,
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}
