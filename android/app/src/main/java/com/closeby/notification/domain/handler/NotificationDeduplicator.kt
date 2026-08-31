package com.closeby.notification.domain.handler

import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationType
import com.closeby.notification.domain.repository.NotificationRepository

/**
 * Prevents duplicate notifications for the same logical event.
 */
class NotificationDeduplicator {

    private val recentKeys = LinkedHashSet<String>()
    private val maxKeys = 500

    fun isDuplicate(eventKey: String): Boolean {
        if (eventKey in recentKeys) return true
        recentKeys.add(eventKey)
        while (recentKeys.size > maxKeys) {
            val first = recentKeys.first()
            recentKeys.remove(first)
        }
        return false
    }

    suspend fun existsInRepository(
        repository: NotificationRepository,
        userId: String,
        type: NotificationType,
        referenceType: String?,
        referenceId: String?
    ): Boolean {
        if (referenceId.isNullOrBlank()) return false
        return repository.getNotifications(userId).getOrNull()
            ?.any {
                it.type == type &&
                    it.referenceType == referenceType &&
                    it.referenceId == referenceId
            } == true
    }

    companion object {
        fun eventKey(
            userId: String,
            type: NotificationType,
            referenceType: String?,
            referenceId: String?
        ): String = listOf(userId, type.name, referenceType.orEmpty(), referenceId.orEmpty()).joinToString(":")
    }
}
