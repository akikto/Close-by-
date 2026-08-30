package com.closeby.app.core.phone

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the device's native phone dialer pre-filled with a number.
 *
 * Uses ACTION_DIAL (not ACTION_CALL), which hands the user off to the
 * system Dialer app to place the call themselves — this requires no
 * CALL_PHONE runtime permission and keeps calling entirely outside the
 * app, per the "no in-app calling" rule.
 */
interface PhoneDialer {
    fun openDialer(phoneNumber: String)
}

class AndroidPhoneDialer(private val context: Context) : PhoneDialer {
    override fun openDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
