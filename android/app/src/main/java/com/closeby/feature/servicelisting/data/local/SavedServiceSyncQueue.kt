package com.closeby.feature.servicelisting.data.local

import android.content.Context

/**
 * Queues saved-service mutations for sync when connectivity returns.
 */
class SavedServiceSyncQueue(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun enqueueSave(serviceId: String) {
        val pending = readPending().toMutableSet()
        pending.remove("-$serviceId")
        pending.add("+$serviceId")
        persist(pending)
    }

    fun enqueueUnsave(serviceId: String) {
        val pending = readPending().toMutableSet()
        pending.remove("+$serviceId")
        pending.add("-$serviceId")
        persist(pending)
    }

    fun drain(): List<PendingMutation> =
        readPending().mapNotNull { token ->
            when {
                token.startsWith("+") -> PendingMutation.Save(token.removePrefix("+"))
                token.startsWith("-") -> PendingMutation.Unsave(token.removePrefix("-"))
                else -> null
            }
        }

    fun clear() {
        prefs.edit().remove(KEY_PENDING).apply()
    }

    fun isEmpty(): Boolean = readPending().isEmpty()

    private fun readPending(): Set<String> =
        prefs.getStringSet(KEY_PENDING, emptySet()).orEmpty()

    private fun persist(tokens: Set<String>) {
        prefs.edit().putStringSet(KEY_PENDING, tokens).apply()
    }

    sealed class PendingMutation(val serviceId: String) {
        class Save(serviceId: String) : PendingMutation(serviceId)
        class Unsave(serviceId: String) : PendingMutation(serviceId)
    }

    companion object {
        private const val PREFS = "closeby_saved_sync_queue"
        private const val KEY_PENDING = "pending"
    }
}
