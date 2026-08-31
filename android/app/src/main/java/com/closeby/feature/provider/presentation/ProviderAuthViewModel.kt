package com.closeby.feature.provider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object SignedOut : AuthUiState()
    data object SendingOtp : AuthUiState()
    data object AwaitingOtp : AuthUiState()
    data object Verifying : AuthUiState()
    data class SignedIn(val providerId: String, val userId: String, val email: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class ProviderAuthViewModel(
    private val authRepository: AuthRepository,
    private val providerRepository: ProviderManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.SignedOut)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var pendingEmail: String? = null

    init {
        viewModelScope.launch { restoreSession() }
    }

    fun sendOtp(email: String) {
        _uiState.value = AuthUiState.SendingOtp
        viewModelScope.launch {
            authRepository.sendEmailOtp(email)
                .onSuccess {
                    pendingEmail = email.trim()
                    _uiState.value = AuthUiState.AwaitingOtp
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Could not send code.")
                }
        }
    }

    fun verifyOtp(token: String) {
        val email = pendingEmail ?: return
        _uiState.value = AuthUiState.Verifying
        viewModelScope.launch {
            authRepository.verifyEmailOtp(email, token)
                .onSuccess { session ->
                    val providerId = providerRepository.ensureProviderForUser(
                        userId = session.userId,
                        email = session.email,
                        defaultName = session.email.substringBefore("@")
                    ).getOrElse {
                        _uiState.value = AuthUiState.Error(it.message ?: "Could not link provider account.")
                        return@launch
                    }
                    _uiState.value = AuthUiState.SignedIn(
                        providerId = providerId,
                        userId = session.userId,
                        email = session.email
                    )
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Invalid verification code.")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            pendingEmail = null
            _uiState.value = AuthUiState.SignedOut
        }
    }

    private suspend fun restoreSession() {
        val session = authRepository.getCurrentSession() ?: return
        val providerId = providerRepository.getProviderIdForUser(session.userId).getOrNull()
            ?: providerRepository.ensureProviderForUser(
                session.userId,
                session.email,
                session.email.substringBefore("@")
            ).getOrNull()
        if (providerId != null) {
            _uiState.value = AuthUiState.SignedIn(
                providerId = providerId,
                userId = session.userId,
                email = session.email
            )
        }
    }
}
