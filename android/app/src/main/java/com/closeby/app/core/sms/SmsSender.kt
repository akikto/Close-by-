package com.closeby.app.core.sms

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the device's native SMS app pre-filled with a recipient and
 * optional message body.
 *
 * Uses ACTION_SENDTO with an "smsto:" URI, which hands off to the
 * system Messages app to send the text itself — this requires no
 * SEND_SMS runtime permission and keeps messaging entirely outside the
 * app, per the "no in-app SMS/OTP" rule.
 */
interface SmsSender {
    fun openSms(phoneNumber: String, message: String? = null)
}

class AndroidSmsSender(private val context: Context) : SmsSender {
    override fun openSms(phoneNumber: String, message: String?) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
            message?.let { putExtra("sms_body", it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
