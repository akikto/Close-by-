package com.closeby.app.data.auth

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.app.data.model.AccountDeletionInsertDto
import com.closeby.app.data.model.AccountDeletionRequestDto
import com.closeby.app.domain.auth.AccountDeletionRequest
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.app.domain.auth.AuthSession
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.postgrest.from
import java.time.Instant

class SupabaseAuthRepository(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) : AuthRepository {

    override suspend fun sendEmailOtp(email: String): Result<Unit> = runCatching {
        client.auth.signInWith(OTP) {
            this.email = email.trim()
        }
    }

    override suspend fun verifyEmailOtp(email: String, token: String): Result<AuthSession> =
        runCatching {
            client.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = email.trim(),
                token = token.trim()
            )
            val user = client.auth.currentUserOrNull()
                ?: error("Verification succeeded but no user session was created.")
            AuthSession(
                userId = user.id,
                email = user.email ?: email.trim()
            )
        }

    override suspend fun getCurrentSession(): AuthSession? {
        val user = client.auth.currentUserOrNull() ?: return null
        return AuthSession(
            userId = user.id,
            email = user.email.orEmpty()
        )
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        client.auth.signOut()
    }

    override suspend fun requestAccountDeletion(reason: String?): Result<AccountDeletionRequest> =
        runCatching {
            val userId = getCurrentSession()?.userId
                ?: throw IllegalStateException("You must be signed in to delete your account.")
            val dto = client.from("account_deletion_requests")
                .insert(AccountDeletionInsertDto(userId = userId, reason = reason?.trim())) {
                    select()
                }
                .decodeSingle<AccountDeletionRequestDto>()
            AccountDeletionRequest(
                id = dto.id,
                userId = dto.userId,
                status = dto.status,
                requestedAt = Instant.parse(dto.requestedAt).toEpochMilli()
            )
        }
}
