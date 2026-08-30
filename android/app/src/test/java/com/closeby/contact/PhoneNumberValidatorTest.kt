package com.closeby.contact

import com.closeby.contact.domain.PhoneNumberValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberValidatorTest {

    @Test
    fun `plain local number is valid`() {
        assertTrue(PhoneNumberValidator.isValid("01712345678"))
    }

    @Test
    fun `international number with plus is valid`() {
        assertTrue(PhoneNumberValidator.isValid("+8801712345678"))
    }

    @Test
    fun `number with spaces and dashes is valid`() {
        assertTrue(PhoneNumberValidator.isValid("+880 171-234-5678"))
    }

    @Test
    fun `empty string is invalid`() {
        assertFalse(PhoneNumberValidator.isValid(""))
    }

    @Test
    fun `blank string is invalid`() {
        assertFalse(PhoneNumberValidator.isValid("   "))
    }

    @Test
    fun `letters make it invalid`() {
        assertFalse(PhoneNumberValidator.isValid("call-me-maybe"))
    }

    @Test
    fun `too few digits is invalid`() {
        assertFalse(PhoneNumberValidator.isValid("12"))
    }

    @Test
    fun `too many digits is invalid`() {
        assertFalse(PhoneNumberValidator.isValid("1234567890123456"))
    }

    @Test
    fun `normalize strips formatting but keeps leading plus`() {
        assertEquals("+8801712345678", PhoneNumberValidator.normalize("+880 171-234-5678"))
    }

    @Test
    fun `normalize strips formatting without plus`() {
        assertEquals("01712345678", PhoneNumberValidator.normalize("017 1234 5678"))
    }
}
