package com.closeby.app.feature.provider

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.NearbyDependenciesFactory
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.app.core.di.TrustDependenciesFactory
import com.closeby.feature.provider.presentation.ProviderProfileViewModel
import com.closeby.feature.provider.ui.ProviderProfileError
import com.closeby.feature.provider.ui.ProviderProfileLoading
import com.closeby.feature.provider.ui.ProviderProfileScreen
import com.closeby.util.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileRoute(
    providerId: String,
    onBack: () -> Unit,
    onMyServices: () -> Unit,
    onEditAvailability: () -> Unit,
    onServiceClick: (String) -> Unit,
    onProviderRequests: () -> Unit,
    onVerification: () -> Unit = {},
    onReportProvider: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stack = remember(context) { NearbyDependenciesFactory.createStack(context) }
    val trustRepository = remember(context) { TrustDependenciesFactory.trustRepository(context) }
    var resolvedOwnProviderId by remember { mutableStateOf<String?>(null) }
    var isBlocked by remember { mutableStateOf(false) }
    LaunchedEffect(providerId) {
        val session = ProviderDependenciesFactory.authRepository().getCurrentSession()
        resolvedOwnProviderId = session?.let {
            ProviderDependenciesFactory.providerManagementRepository()
                .getProviderIdForUser(it.userId)
                .getOrNull()
        }
        session?.userId?.let { userId ->
            isBlocked = trustRepository.isProviderBlocked(userId, providerId).getOrDefault(false)
        }
    }
    val viewModel: ProviderProfileViewModel = viewModel(
        factory = remember(providerId, resolvedOwnProviderId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProviderProfileViewModel(
                        providerId = providerId,
                        currentProviderId = resolvedOwnProviderId,
                        repository = ProviderDependenciesFactory.providerManagementRepository(),
                        availabilityRepository = ProviderDependenciesFactory.availabilityRepository(),
                        locationSession = stack.locationSession
                    ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(providerId) { viewModel.load() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Provider Profile") },
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
                is UiState.Idle, is UiState.Loading -> ProviderProfileLoading()
                is UiState.Success -> ProviderProfileScreen(
                    profile = state.data,
                    onMyServices = onMyServices,
                    onEditAvailability = onEditAvailability,
                    onServiceClick = onServiceClick,
                    onProviderRequests = onProviderRequests,
                    onVerification = onVerification,
                    onReportProvider = onReportProvider,
                    isBlocked = isBlocked,
                    onBlockProvider = {
                        scope.launch {
                            val session = ProviderDependenciesFactory.authRepository().getCurrentSession()
                                ?: return@launch
                            if (isBlocked) {
                                trustRepository.unblockProvider(session.userId, providerId)
                            } else {
                                trustRepository.blockProvider(session.userId, providerId)
                            }.onSuccess { isBlocked = !isBlocked }
                        }
                    }
                )
                is UiState.Error -> ProviderProfileError(state.message, onRetry = viewModel::load)
            }
        }
    }
}
