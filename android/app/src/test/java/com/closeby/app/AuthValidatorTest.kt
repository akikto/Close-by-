package com.closeby.app

import com.closeby.app.domain.auth.AuthValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validEmailAccepted() {
        assertTrue(AuthValidator.validateEmail("user@example.com").isSuccess)
    }

    @Test
    fun invalidEmailRejected() {
        assertFalse(AuthValidator.validateEmail("not-an-email").isSuccess)
        assertFalse(AuthValidator.validateEmail("").isSuccess)
    }

    @Test
    fun shortOtpRejected() {
        assertFalse(AuthValidator.validateOtp("12").isSuccess)
    }

    @Test
    fun validOtpAccepted() {
        assertTrue(AuthValidator.validateOtp("123456").isSuccess)
    }
}
