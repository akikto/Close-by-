package com.closeby.app.data.auth

import com.closeby.app.domain.auth.AuthRepository
import com.closeby.app.domain.auth.AuthSession

/**
 * Demo auth for builds without Supabase credentials. Accepts any 6-digit OTP.
 */
class MockAuthRepository : AuthRepository {

    private var session: AuthSession? = null

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
        return Result.success(Unit)
    }
}
