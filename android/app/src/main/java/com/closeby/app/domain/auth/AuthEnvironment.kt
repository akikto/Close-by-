package com.closeby.app.domain.auth

import com.closeby.app.BuildConfig

/**
 * Whether the app talks to a real Supabase project or falls back to demo auth.
 * CI/debug APKs built without `local.properties` credentials use demo auth —
 * OTP emails are not sent in that mode.
 */
object AuthEnvironment {
    val usesSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    /** Suggested code for demo builds (any 4+ digit code also works). */
    const val DEMO_OTP_CODE = "123456"
}
