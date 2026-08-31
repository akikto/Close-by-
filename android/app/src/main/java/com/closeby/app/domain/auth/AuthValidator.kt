package com.closeby.app.domain.auth

object AuthValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): Result<Unit> =
        if (email.isBlank() || !EMAIL_REGEX.matches(email.trim())) {
            Result.failure(IllegalArgumentException("Enter a valid email address."))
        } else {
            Result.success(Unit)
        }

    fun validateOtp(token: String): Result<Unit> =
        if (token.trim().length < 4) {
            Result.failure(IllegalArgumentException("Enter the verification code from your email."))
        } else {
            Result.success(Unit)
        }
}
