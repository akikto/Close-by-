package com.closeby.app.core.session

/**
 * Stable per-install identifier used to scope anonymous customer requests.
 * Paired with [client_session_id] on service_requests and the
 * x-client-session-id request header for RLS (see schema_phase4.sql).
 */
interface ClientSessionStorage {
    suspend fun getOrCreateSessionId(): String
    suspend fun rememberRequestId(requestId: String)
    suspend fun getRememberedRequestIds(): Set<String>
}

object ClientSessionHolder {
  @Volatile
  var sessionId: String = ""
}
