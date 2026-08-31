package com.closeby.app.core.error

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPolicyTest {

    @Test
    fun succeedsOnFirstAttempt() = runTest {
        var attempts = 0
        val result = retryWithBackoff(times = 3) {
            attempts++
            Result.success("ok")
        }
        assertTrue(result.isSuccess)
        assertEquals("ok", result.getOrNull())
        assertEquals(1, attempts)
    }

    @Test
    fun retriesUntilSuccess() = runTest {
        var attempts = 0
        val result = retryWithBackoff(times = 3, initialDelayMs = 1) {
            attempts++
            if (attempts < 3) Result.failure(IllegalStateException("fail"))
            else Result.success("ok")
        }
        assertTrue(result.isSuccess)
        assertEquals(3, attempts)
    }
}
