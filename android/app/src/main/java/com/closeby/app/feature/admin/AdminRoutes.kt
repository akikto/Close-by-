package com.closeby.app.feature.admin

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.admin.presentation.AdminAdvertisementsViewModel
import com.closeby.admin.presentation.AdminDashboardViewModel
import com.closeby.admin.presentation.AdminGateViewModel
import com.closeby.admin.presentation.AdminProvidersViewModel
import com.closeby.admin.presentation.AdminReportsViewModel
import com.closeby.admin.presentation.AdminServicesViewModel
import com.closeby.admin.presentation.AdminUsersViewModel
import com.closeby.admin.presentation.AdminVerificationViewModel
import com.closeby.admin.ui.AdminAdvertisementsScreen
import com.closeby.admin.ui.AdminGateScreen
import com.closeby.admin.ui.AdminProvidersScreen
import com.closeby.admin.ui.AdminReportsScreen
import com.closeby.admin.ui.AdminServicesScreen
import com.closeby.admin.ui.AdminUsersScreen
import com.closeby.admin.ui.AdminVerificationScreen
import com.closeby.app.core.di.AdminDependenciesFactory
import com.closeby.trust.domain.model.ReportStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGateRoute(
    userId: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { AdminDependenciesFactory.adminRepository() }
    val gateViewModel: AdminGateViewModel = viewModel(
        key = userId,
        factory = remember(userId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminGateViewModel(userId, repository) as T
            }
        }
    )
    val dashboardViewModel: AdminDashboardViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminDashboardViewModel(repository) as T
            }
        }
    )

    val gateState by gateViewModel.uiState.collectAsState()
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    LaunchedEffect(gateState) {
        if (gateState is com.closeby.admin.presentation.AdminGateUiState.Authorized) {
            dashboardViewModel.load()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Admin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AdminGateScreen(
            gateState = gateState,
            dashboardState = dashboardState,
            onRetry = gateViewModel::checkAccess,
            onNavigate = onNavigate,
            onRefreshDashboard = dashboardViewModel::load,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVerificationRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = remember { AdminDependenciesFactory.adminRepository() }
    val viewModel: AdminVerificationViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminVerificationViewModel(repository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Verifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AdminVerificationScreen(
            uiState = uiState,
            selectedTab = selectedTab,
            onTabSelected = viewModel::selectTab,
            onApprove = viewModel::approve,
            onReject = { viewModel.reject(it, "Rejected by admin") },
            onSuspend = viewModel::suspendProvider,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = remember { AdminDependenciesFactory.adminRepository() }
    val viewModel: AdminReportsViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminReportsViewModel(repository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AdminReportsScreen(
            uiState = uiState,
            onResolve = { viewModel.updateStatus(it, ReportStatus.RESOLVED) },
            onDismiss = { viewModel.updateStatus(it, ReportStatus.DISMISSED) },
            onReview = { viewModel.updateStatus(it, ReportStatus.UNDER_REVIEW) },
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdvertisementsRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = remember { AdminDependenciesFactory.adminRepository() }
    val viewModel: AdminAdvertisementsViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminAdvertisementsViewModel(repository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Advertisements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AdminAdvertisementsScreen(
            uiState = uiState,
            onApprove = viewModel::approve,
            onReject = viewModel::reject,
            onPause = viewModel::pause,
            onResume = viewModel::resume,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProvidersRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = remember { AdminDependenciesFactory.adminRepository() }
    val viewModel: AdminProvidersViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminProvidersViewModel(repository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Providers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AdminProvidersScreen(
            uiState = uiState,
            searchQuery = searchQuery,
            onSearchChange = viewModel::updateSearch,
            onSuspend = viewModel::suspendProvider,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServicesRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = remember { AdminDependenciesFactory.adminRepository() }
    val viewModel: AdminServicesViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminServicesViewModel(repository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Services") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AdminServicesScreen(
            uiState = uiState,
            onToggle = viewModel::toggleService,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = remember { AdminDependenciesFactory.adminRepository() }
    val viewModel: AdminUsersViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminUsersViewModel(repository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Users") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AdminUsersScreen(
            uiState = uiState,
            onToggleSuspend = viewModel::toggleSuspend,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun AdminLoadingRoute(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
