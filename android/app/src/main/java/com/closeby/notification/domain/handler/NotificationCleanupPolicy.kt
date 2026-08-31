package com.closeby.notification.domain.handler

import java.util.concurrent.TimeUnit

/**
 * Retains notifications for a bounded period; older read notifications may be pruned client-side.
 */
object NotificationCleanupPolicy {
    const val RETENTION_DAYS = 90L

    fun shouldRetain(notificationCreatedAtMs: Long, isRead: Boolean, nowMs: Long = System.currentTimeMillis()): Boolean {
        val ageMs = nowMs - notificationCreatedAtMs
        val maxAgeMs = TimeUnit.DAYS.toMillis(RETENTION_DAYS)
        return !isRead || ageMs <= maxAgeMs
    }
}
