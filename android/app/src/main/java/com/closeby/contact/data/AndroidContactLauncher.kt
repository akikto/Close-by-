package com.closeby.contact.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.closeby.contact.domain.ContactError
import com.closeby.contact.domain.ContactLauncher
import com.closeby.contact.domain.PhoneNumberValidator

/**
 * Android implementation of [ContactLauncher].
 *
 * Uses ACTION_DIAL (never ACTION_CALL) and ACTION_SENDTO with a "smsto:"
 * scheme so no CALL_PHONE / SEND_SMS permission is required — the user
 * always takes the final action (tap Call / tap Send) inside the native
 * dialer/SMS app, not inside Close by.
 */
class AndroidContactLauncher(
    private val context: Context
) : ContactLauncher {

    override fun call(phoneNumber: String): Result<Unit> {
        if (!PhoneNumberValidator.isValid(phoneNumber)) {
            return Result.failure(ContactError.InvalidPhoneNumber(phoneNumber))
        }

        return try {
            val uri = Uri.parse("tel:${PhoneNumberValidator.normalize(phoneNumber)}")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: ActivityNotFoundException) {
            Result.failure(ContactError.PhoneAppUnavailable)
        } catch (e: Exception) {
            Result.failure(ContactError.PhoneAppUnavailable)
        }
    }

    override fun sms(phoneNumber: String, message: String?): Result<Unit> {
        if (!PhoneNumberValidator.isValid(phoneNumber)) {
            return Result.failure(ContactError.InvalidPhoneNumber(phoneNumber))
        }

        return try {
            val uri = Uri.parse("smsto:${PhoneNumberValidator.normalize(phoneNumber)}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (!message.isNullOrBlank()) {
                    putExtra("sms_body", message)
                }
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: ActivityNotFoundException) {
            Result.failure(ContactError.SmsAppUnavailable)
        } catch (e: Exception) {
            Result.failure(ContactError.SmsAppUnavailable)
        }
    }

    companion object {
        const val DEFAULT_SMS_MESSAGE = "Hi, I found your service on Close by."
    }
}
