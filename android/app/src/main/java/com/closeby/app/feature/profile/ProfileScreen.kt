package com.closeby.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.closeby.app.core.di.AdminDependenciesFactory
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.feature.provider.presentation.AuthUiState
import com.closeby.feature.provider.presentation.ProviderAuthViewModel

@Composable
fun ProfileScreen(
    onProviderProfile: (providerId: String) -> Unit,
    onMyAdvertisements: (String) -> Unit,
    onCreateAdvertisement: (String) -> Unit,
    onAdminDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ProviderAuthViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProviderAuthViewModel(
                        authRepository = ProviderDependenciesFactory.authRepository(),
                        providerRepository = ProviderDependenciesFactory.providerManagementRepository()
                    ) as T
            }
        }
    )
    val authState by viewModel.uiState.collectAsState()
    var isAdmin by remember { mutableStateOf<Boolean?>(null) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        val signedIn = authState as? AuthUiState.SignedIn
        isAdmin = if (signedIn != null) {
            withContext(Dispatchers.IO) {
                AdminDependenciesFactory.adminRepository().isAdmin(signedIn.userId)
            }
        } else {
            null
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Browse services without signing in. Providers can sign in with Email OTP to manage listings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = authState) {
                is AuthUiState.SignedIn -> {
                    Text("Signed in as ${state.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onProviderProfile(state.providerId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Provider Profile")
                    }
                    if (isAdmin == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onAdminDashboard,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Admin Dashboard")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onMyAdvertisements(state.userId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("My Advertisements")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onCreateAdvertisement(state.userId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Create Advertisement")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = viewModel::signOut,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Sign out")
                    }
                }
                is AuthUiState.SendingOtp, is AuthUiState.Verifying -> {
                    CircularProgressIndicator()
                }
                is AuthUiState.AwaitingOtp -> {
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
                    ) {
                        Text("Verify & sign in")
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Provider email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.sendOtp(email) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Send Email OTP")
                    }
                    if (state is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
