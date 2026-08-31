package com.closeby.app.core.session

/**
 * Stable per-install identifier used to scope anonymous customer requests.
 * The app stores created request UUIDs locally and loads them via getByIds when
 * the user is not signed in (see schema_phase4.sql).
 */
interface ClientSessionStorage {
    suspend fun getOrCreateSessionId(): String
    suspend fun rememberRequestId(requestId: String)
    suspend fun getRememberedRequestIds(): Set<String>
    suspend fun cacheRequest(request: com.closeby.request.domain.model.ServiceRequest)
    suspend fun getCachedRequests(): List<com.closeby.request.domain.model.ServiceRequest>
    suspend fun updateCachedRequest(request: com.closeby.request.domain.model.ServiceRequest)
}

object ClientSessionHolder {
  @Volatile
  var sessionId: String = ""
}
