package com.closeby.app.data.auth

import com.closeby.app.domain.auth.AccountDeletionRequest
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.app.domain.auth.AuthSession

/**
 * Demo auth for builds without Supabase credentials.
 * No email is sent — use [com.closeby.app.domain.auth.AuthEnvironment.DEMO_OTP_CODE]
 * or any 4+ digit code to verify.
 */
class MockAuthRepository : AuthRepository {

    private var session: AuthSession? = null
    private var deletionRequested = false

    override suspend fun sendEmailOtp(email: String): Result<Unit> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        return Result.success(Unit)
    }

    override suspend fun verifyEmailOtp(email: String, token: String): Result<AuthSession> {
        if (token.length < 4) {
            return Result.failure(IllegalArgumentException("Enter the verification code from your email."))
        }
        val created = AuthSession(
            userId = "mock-user-${email.hashCode()}",
            email = email.trim()
        )
        session = created
        return Result.success(created)
    }

    override suspend fun getCurrentSession(): AuthSession? = session

    override suspend fun signOut(): Result<Unit> {
        session = null
        deletionRequested = false
        return Result.success(Unit)
    }

    override suspend fun requestAccountDeletion(reason: String?): Result<AccountDeletionRequest> {
        val current = session ?: return Result.failure(IllegalStateException("Not signed in."))
        if (deletionRequested) {
            return Result.failure(IllegalStateException("Deletion already requested."))
        }
        deletionRequested = true
        return Result.success(
            AccountDeletionRequest(
                id = "mock-deletion-${current.userId}",
                userId = current.userId,
                status = "PENDING",
                requestedAt = System.currentTimeMillis()
            )
        )
    }
}
