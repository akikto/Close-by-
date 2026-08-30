package com.closeby.contact.domain

/**
 * Contract for launching the device's native phone/SMS apps.
 *
 * Close by never places calls or sends SMS itself — it only hands off to
 * the OS via Intent.ACTION_DIAL / Intent.ACTION_SENDTO. No CALL_PHONE or
 * SEND_SMS permission is ever requested.
 */
interface ContactLauncher {

    /**
     * Opens the device dialer pre-filled with [phoneNumber].
     * Does NOT place the call automatically (ACTION_DIAL, not ACTION_CALL).
     */
    fun call(phoneNumber: String): Result<Unit>

    /**
     * Opens the device's default SMS app pre-filled with [phoneNumber]
     * and an optional draft [message]. Does NOT send the SMS automatically.
     */
    fun sms(phoneNumber: String, message: String? = null): Result<Unit>
}

/**
 * Marker/base error types so callers (ViewModels) can show friendly
 * messages instead of leaking exception internals.
 */
sealed class ContactError(message: String) : Exception(message) {
    data class InvalidPhoneNumber(val raw: String) :
        ContactError("The phone number \"$raw\" is not valid.")

    data object PhoneAppUnavailable :
        ContactError("Phone app is not available on this device.")

    data object SmsAppUnavailable :
        ContactError("SMS app is not available on this device.")
}
