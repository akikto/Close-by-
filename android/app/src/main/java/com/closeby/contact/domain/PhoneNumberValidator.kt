package com.closeby.contact.domain

/**
 * Minimal, dependency-free phone number sanity check.
 *
 * This is intentionally loose — Close by does not verify numbers, it only
 * rejects strings that are obviously not phone numbers before handing them
 * to the dialer/SMS intent, so we fail fast with a clear error instead of
 * throwing inside an Activity.
 */
object PhoneNumberValidator {

    private val ALLOWED_CHARS = Regex("^[0-9+()\\-\\s]+$")

    fun isValid(rawNumber: String): Boolean {
        val trimmed = rawNumber.trim()
        if (trimmed.isEmpty()) return false
        if (!ALLOWED_CHARS.matches(trimmed)) return false

        val digitCount = trimmed.count { it.isDigit() }
        return digitCount in 4..15
    }

    /** Strips everything except digits and a leading '+', for building the tel:/smsto: URI. */
    fun normalize(rawNumber: String): String {
        val trimmed = rawNumber.trim()
        val hasLeadingPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }
        return if (hasLeadingPlus) "+$digits" else digits
    }
}
