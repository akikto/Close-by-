package com.closeby.contact

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.closeby.contact.data.AndroidContactLauncher
import com.closeby.contact.domain.ContactError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * NOTE: These tests use Robolectric to verify Intent construction without
 * an emulator. If the host project does not already depend on Robolectric
 * (org.robolectric:robolectric) for `testImplementation`, add it, or move
 * this file to `androidTest` and run it as an instrumented test instead.
 * PhoneNumberValidatorTest and ServiceRequestValidatorTest do not need
 * Robolectric and will run on plain JVM regardless.
 */
@RunWith(RobolectricTestRunner::class)
class AndroidContactLauncherTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val launcher = AndroidContactLauncher(context)

    @Test
    fun `call launches ACTION_DIAL with tel uri, never ACTION_CALL`() {
        val result = launcher.call("+8801712345678")
        assertTrue(result.isSuccess)

        val started = shadowOf(context as android.app.Application).nextStartedActivity
        assertEquals(Intent.ACTION_DIAL, started.action)
        assertEquals("tel", started.data?.scheme)
        assertTrue(started.data.toString().contains("8801712345678"))
    }

    @Test
    fun `sms launches ACTION_SENDTO with smsto uri`() {
        val result = launcher.sms("+8801712345678", "Hi, I found your service on Close by.")
        assertTrue(result.isSuccess)

        val started = shadowOf(context as android.app.Application).nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, started.action)
        assertEquals("smsto", started.data?.scheme)
        assertEquals(
            "Hi, I found your service on Close by.",
            started.getStringExtra("sms_body")
        )
    }

    @Test
    fun `invalid phone number fails before any intent is built`() {
        val result = launcher.call("not-a-number")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ContactError.InvalidPhoneNumber)
    }
}
