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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.feature.provider.presentation.AddEditServiceViewModel
import com.closeby.feature.provider.presentation.ServiceFormUiState
import com.closeby.feature.provider.ui.AddEditServiceScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditServiceRoute(
    providerId: String,
    serviceId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AddEditServiceViewModel = viewModel(
        factory = remember(providerId, serviceId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AddEditServiceViewModel(
                        providerId = providerId,
                        serviceId = serviceId,
                        repository = ProviderDependenciesFactory.providerManagementRepository(),
                        imageUploader = ProviderDependenciesFactory.imageUploader(context)
                    ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(serviceId) { viewModel.load() }
    LaunchedEffect(uiState) {
        if (uiState is ServiceFormUiState.Saved) {
            onSaved((uiState as ServiceFormUiState.Saved).serviceId)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (serviceId == null) "Add Service" else "Edit Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AddEditServiceScreen(
            state = uiState,
            onSave = viewModel::save,
            onUpdate = viewModel::updateForm,
            onPickImage = viewModel::queueImage,
            onRemoveImage = viewModel::removeImage,
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}
