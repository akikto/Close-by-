package com.closeby.app.core.session

import android.content.Context
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

    companion object {
        private const val PREFS_NAME = "closeby_client_session"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_REQUEST_IDS = "request_ids"
    }
}
