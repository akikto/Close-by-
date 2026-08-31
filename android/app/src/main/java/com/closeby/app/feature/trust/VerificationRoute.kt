package com.closeby.app.feature.trust

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.app.core.di.TrustDependenciesFactory
import com.closeby.trust.presentation.VerificationViewModel
import com.closeby.trust.ui.VerificationScreen
import com.closeby.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationRoute(
    providerId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var submittedBy by remember { mutableStateOf("anonymous") }
    LaunchedEffect(Unit) {
        submittedBy = ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId ?: "anonymous"
    }

    val viewModel: VerificationViewModel = viewModel(
        factory = remember(providerId, submittedBy) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    VerificationViewModel(
                        providerId = providerId,
                        submittedBy = submittedBy,
                        trustRepository = TrustDependenciesFactory.trustRepository(context)
                    ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(providerId) { viewModel.load() }

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is UiState.Success -> snackbarHostState.showSnackbar("Verification submitted.")
            is UiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Provider Verification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Success -> VerificationScreen(
                    form = state.data,
                    onBusinessNameChange = viewModel::updateBusinessName,
                    onContactPhoneChange = viewModel::updateContactPhone,
                    onDescriptionChange = viewModel::updateDescription,
                    onDocumentUrlChange = viewModel::updateDocumentUrl,
                    onSubmit = viewModel::submit
                )
                is UiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
