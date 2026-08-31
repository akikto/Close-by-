package com.closeby.app.domain.auth

data class AuthSession(
    val userId: String,
    val email: String
)

data class AccountDeletionRequest(
    val id: String,
    val userId: String,
    val status: String,
    val requestedAt: Long
)

interface AuthRepository {
    suspend fun sendEmailOtp(email: String): Result<Unit>
    suspend fun verifyEmailOtp(email: String, token: String): Result<AuthSession>
    suspend fun getCurrentSession(): AuthSession?
    suspend fun signOut(): Result<Unit>
    suspend fun requestAccountDeletion(reason: String? = null): Result<AccountDeletionRequest>
}
