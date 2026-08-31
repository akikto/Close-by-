package com.closeby.app.feature.trust

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.closeby.trust.domain.model.ReportTargetType
import com.closeby.trust.presentation.ReportViewModel
import com.closeby.trust.ui.ReportScreen
import com.closeby.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportRoute(
    targetType: ReportTargetType,
    targetId: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit = onBack,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var reporterId by remember { mutableStateOf("anonymous") }
    LaunchedEffect(Unit) {
        reporterId = ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId ?: "anonymous"
    }

    val viewModel: ReportViewModel = viewModel(
        factory = remember(targetType, targetId, reporterId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReportViewModel(
                        reporterId = reporterId,
                        targetType = targetType,
                        targetId = targetId,
                        trustRepository = TrustDependenciesFactory.trustRepository(context)
                    ) as T
            }
        }
    )
    val formState by viewModel.formState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Report submitted.")
                onSubmitted()
            }
            is UiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Report") },
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
            ReportScreen(
                form = formState,
                onReasonSelected = viewModel::selectReason,
                onDescriptionChange = viewModel::updateDescription,
                onSubmit = viewModel::submit,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
