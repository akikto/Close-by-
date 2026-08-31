package com.closeby.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.AdminDependenciesFactory
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.app.core.di.SavedDependenciesFactory
import com.closeby.app.domain.auth.AuthState
import com.closeby.app.feature.saved.MigrationPromptState
import com.closeby.app.feature.saved.SavedServiceMigrationDialog
import com.closeby.app.feature.saved.SavedServiceMigrationManager
import com.closeby.feature.provider.presentation.AccountAuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    onProviderProfile: (providerId: String) -> Unit,
    onMyAdvertisements: (String) -> Unit,
    onCreateAdvertisement: (String) -> Unit,
    onAdminDashboard: () -> Unit,
    onMyRequests: () -> Unit = {},
    onSavedServices: () -> Unit = {},
    onRecentlyViewed: () -> Unit = {},
    onBlockedProviders: () -> Unit = {},
    onSettings: () -> Unit = {},
    onReportProblem: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authRepository = remember { ProviderDependenciesFactory.authRepository() }
    val migrationManager = remember {
        SavedDependenciesFactory.migrationManager(context, authRepository)
    }
    val migrationState by migrationManager.state.collectAsState()

    val viewModel: AccountAuthViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AccountAuthViewModel(
                        authRepository = authRepository,
                        providerRepository = ProviderDependenciesFactory.providerManagementRepository(),
                        onUserSignedIn = migrationManager::onSignedIn
                    ) as T
            }
        }
    )
    val authState by viewModel.authState.collectAsState()
    var isAdmin by remember { mutableStateOf<Boolean?>(null) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    SavedServiceMigrationDialog(
        state = migrationState,
        onConfirm = {
            val prompt = migrationState as? MigrationPromptState.Prompt ?: return@SavedServiceMigrationDialog
            viewModel.viewModelScope.launch {
                migrationManager.migrate(
                    prompt.userId,
                    SavedDependenciesFactory.localSavedRepository(context).currentIds()
                )
            }
        },
        onDismiss = migrationManager::dismiss,
        onRetry = {
            val error = migrationState as? MigrationPromptState.Error ?: return@SavedServiceMigrationDialog
            viewModel.viewModelScope.launch {
                migrationManager.retry(error.userId, error.localIds)
            }
        },
        onSuccessDismiss = migrationManager::clearSuccess
    )

    LaunchedEffect(authState) {
        val signedIn = authState as? AuthState.SignedIn
        isAdmin = if (signedIn != null) {
            withContext(Dispatchers.IO) {
                AdminDependenciesFactory.adminRepository().isAdmin(signedIn.session.userId)
            }
        } else {
            null
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = {
                Text(
                    "This submits a deletion request. Your account will be deactivated after review. " +
                        "You will be signed out immediately."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.requestAccountDeletion()
                }) { Text("Delete account") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Account", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Browse services without signing in. Sign in with Email OTP to manage your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = authState) {
                is AuthState.SignedIn -> {
                    val session = state.session
                    Text("Signed in as ${session.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    state.providerId?.let { providerId ->
                        AccountButton("Provider Profile") { onProviderProfile(providerId) }
                        AccountButton("My Services") { onProviderProfile(providerId) }
                        AccountOutlinedButton("Requests") { onMyRequests() }
                    }

                    AccountButton("My Requests") { onMyRequests() }
                    AccountButton("Saved Services") { onSavedServices() }
                    AccountOutlinedButton("Recently Viewed") { onRecentlyViewed() }
                    AccountOutlinedButton("Blocked Providers") { onBlockedProviders() }
                    AccountOutlinedButton("My Advertisements") { onMyAdvertisements(session.userId) }
                    AccountOutlinedButton("Create Advertisement") { onCreateAdvertisement(session.userId) }

                    if (isAdmin == true) {
                        AccountButton("Admin Dashboard") { onAdminDashboard() }
                    }

                    AccountOutlinedButton("Settings") { onSettings() }
                    AccountOutlinedButton("Help") { onReportProblem() }
                    AccountOutlinedButton("Report a Problem") { onReportProblem() }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Delete Account") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = viewModel::signOut,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Sign out") }
                }
                AuthState.Loading -> {
                    CircularProgressIndicator()
                }
                is AuthState.OtpVerification -> {
                    CircularProgressIndicator()
                }
                is AuthState.OtpRequested -> {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("Verification code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.verifyOtp(otp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Verify & sign in") }
                }
                AuthState.SignedOut -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.sendOtp(email) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Send Email OTP") }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Continue browsing without signing in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AuthState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = viewModel::clearError) { Text("Try again") }
                }
            }
        }
    }
}

@Composable
private fun AccountButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp)
    ) { Text(label) }
}

@Composable
private fun AccountOutlinedButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp)
    ) { Text(label) }
}
