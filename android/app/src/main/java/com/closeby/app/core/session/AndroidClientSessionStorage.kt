package com.closeby.app.core.session

import android.content.Context
import com.closeby.request.domain.model.ServiceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AndroidClientSessionStorage(
    context: Context
) : ClientSessionStorage {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getOrCreateSessionId(): String = withContext(Dispatchers.IO) {
        val existing = prefs.getString(KEY_SESSION_ID, null)
        if (existing != null) {
            ClientSessionHolder.sessionId = existing
            return@withContext existing
        }
        val created = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_SESSION_ID, created).apply()
        ClientSessionHolder.sessionId = created
        created
    }

    override suspend fun rememberRequestId(requestId: String) = withContext(Dispatchers.IO) {
        val current = prefs.getStringSet(KEY_REQUEST_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(requestId)
        prefs.edit().putStringSet(KEY_REQUEST_IDS, current).apply()
    }

    override suspend fun getRememberedRequestIds(): Set<String> = withContext(Dispatchers.IO) {
        prefs.getStringSet(KEY_REQUEST_IDS, emptySet()) ?: emptySet()
    }

    override suspend fun cacheRequest(request: ServiceRequest) = withContext(Dispatchers.IO) {
        val current = prefs.getString(KEY_REQUEST_CACHE, "{}") ?: "{}"
        val map = parseCacheMap(current).toMutableMap()
        map[request.id] = ServiceRequestLocalCacheMapper.encode(request)
        prefs.edit().putString(KEY_REQUEST_CACHE, encodeCacheMap(map)).apply()
    }

    override suspend fun getCachedRequests(): List<ServiceRequest> = withContext(Dispatchers.IO) {
        parseCacheMap(prefs.getString(KEY_REQUEST_CACHE, "{}") ?: "{}")
            .values
            .mapNotNull(ServiceRequestLocalCacheMapper::decode)
            .sortedByDescending { it.createdAt }
    }

    override suspend fun updateCachedRequest(request: ServiceRequest) = cacheRequest(request)

    private fun parseCacheMap(raw: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val entries = raw.split(ENTRY_SEPARATOR).filter { it.isNotBlank() }
        for (entry in entries) {
            val parts = entry.split(KEY_VALUE_SEPARATOR, limit = 2)
            if (parts.size == 2) map[parts[0]] = parts[1]
        }
        return map
    }

    private fun encodeCacheMap(map: Map<String, String>): String =
        map.entries.joinToString(ENTRY_SEPARATOR) { "${it.key}$KEY_VALUE_SEPARATOR${it.value}" }

    companion object {
        private const val PREFS_NAME = "closeby_client_session"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_REQUEST_IDS = "request_ids"
        private const val KEY_REQUEST_CACHE = "request_cache"
        private const val ENTRY_SEPARATOR = "\u001E"
        private const val KEY_VALUE_SEPARATOR = "\u001F"
    }
}
