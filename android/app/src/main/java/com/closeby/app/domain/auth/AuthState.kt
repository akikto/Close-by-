package com.closeby.app.domain.auth

/**
 * Canonical authentication UI state for account flows.
 */
sealed interface AuthState {
    data object SignedOut : AuthState
    data object Loading : AuthState
    data class OtpRequested(val email: String) : AuthState
    data class OtpVerification(val email: String) : AuthState
    data class SignedIn(val session: AuthSession, val providerId: String?) : AuthState
    data class Error(val message: String) : AuthState
}
