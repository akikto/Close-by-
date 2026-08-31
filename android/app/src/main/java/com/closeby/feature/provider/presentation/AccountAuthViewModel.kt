package com.closeby.feature.provider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.app.domain.auth.AuthSession
import com.closeby.app.domain.auth.AuthState
import com.closeby.app.domain.auth.AuthValidator
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.notification.domain.handler.NotificationEventPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Production account ViewModel — email OTP auth with provider linking.
 * Anonymous browsing is unaffected; sign-in is optional.
 */
class AccountAuthViewModel(
    private val authRepository: AuthRepository,
    private val providerRepository: ProviderManagementRepository,
    private val onUserSignedIn: ((String) -> Unit)? = null
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var pendingEmail: String? = null

    init {
        viewModelScope.launch { restoreSession() }
    }

    fun sendOtp(email: String) {
        AuthValidator.validateEmail(email).onFailure { error ->
            _authState.value = AuthState.Error(error.message ?: "Invalid email.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.sendEmailOtp(email)
                .onSuccess {
                    pendingEmail = email.trim()
                    _authState.value = AuthState.OtpRequested(email.trim())
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Could not send code.")
                }
        }
    }

    fun verifyOtp(token: String) {
        val email = pendingEmail ?: return
        AuthValidator.validateOtp(token).onFailure { error ->
            _authState.value = AuthState.Error(error.message ?: "Invalid code.")
            return
        }
        _authState.value = AuthState.OtpVerification(email)
        viewModelScope.launch {
            authRepository.verifyEmailOtp(email, token)
                .onSuccess { session -> linkProviderAndSignIn(session) }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Invalid verification code.")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            pendingEmail = null
            _authState.value = AuthState.SignedOut
        }
    }

    fun requestAccountDeletion(reason: String? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentSession()?.userId
            authRepository.requestAccountDeletion(reason)
                .onSuccess {
                    userId?.let { NotificationEventPublisher.accountDeletionRequested(it) }
                    authRepository.signOut()
                    pendingEmail = null
                    _authState.value = AuthState.SignedOut
                    onComplete()
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Could not request deletion.")
                }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = if (pendingEmail != null) {
                AuthState.OtpRequested(pendingEmail!!)
            } else {
                AuthState.SignedOut
            }
        }
    }

    private suspend fun linkProviderAndSignIn(session: AuthSession) {
        val providerId = providerRepository.ensureProviderForUser(
            userId = session.userId,
            email = session.email,
            defaultName = session.email.substringBefore("@")
        ).getOrElse {
            _authState.value = AuthState.Error(it.message ?: "Could not link provider account.")
            return
        }
        _authState.value = AuthState.SignedIn(session = session, providerId = providerId)
        onUserSignedIn?.invoke(session.userId)
    }

    private suspend fun restoreSession() {
        val session = authRepository.getCurrentSession()
        if (session == null) {
            _authState.value = AuthState.SignedOut
            return
        }
        val providerId = providerRepository.getProviderIdForUser(session.userId).getOrNull()
            ?: providerRepository.ensureProviderForUser(
                session.userId,
                session.email,
                session.email.substringBefore("@")
            ).getOrNull()
        _authState.value = AuthState.SignedIn(session = session, providerId = providerId)
    }
}

/** @deprecated Use [AccountAuthViewModel] — kept for gradual migration. */
typealias ProviderAuthViewModel = AccountAuthViewModel

/** @deprecated Use [AuthState] */
sealed class AuthUiState {
    data object SignedOut : AuthUiState()
    data object SendingOtp : AuthUiState()
    data object AwaitingOtp : AuthUiState()
    data object Verifying : AuthUiState()
    data class SignedIn(val providerId: String, val userId: String, val email: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
