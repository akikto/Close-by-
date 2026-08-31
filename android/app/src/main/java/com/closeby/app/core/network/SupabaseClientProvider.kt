package com.closeby.app.core.network

import com.closeby.app.BuildConfig
import com.closeby.app.core.session.ClientSessionHolder
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header

/**
 * Single Supabase client instance for the app.
 *
 * Credentials are never hardcoded here. They come from BuildConfig fields
 * (SUPABASE_URL / SUPABASE_ANON_KEY), which are in turn populated from
 * local.properties / environment variables at build time.
 * See /.env.example and docs/AI_DEVELOPMENT_CONTRACT.md.
 *
 * Auth module is included for the future Email OTP provider-account flow
 * only — it is not wired into any UI in this base project.
 */
object SupabaseClientProvider {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
            httpConfig {
                defaultRequest {
                    val sessionId = ClientSessionHolder.sessionId
                    if (sessionId.isNotBlank()) {
                        header("x-client-session-id", sessionId)
                    }
                }
            }
        }
    }
}
