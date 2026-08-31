package com.closeby.app.domain.auth

data class AuthSession(
    val userId: String,
    val email: String
)

interface AuthRepository {
    suspend fun sendEmailOtp(email: String): Result<Unit>
    suspend fun verifyEmailOtp(email: String, token: String): Result<AuthSession>
    suspend fun getCurrentSession(): AuthSession?
    suspend fun signOut(): Result<Unit>
}
