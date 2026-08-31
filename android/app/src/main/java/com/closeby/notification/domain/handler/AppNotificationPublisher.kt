package com.closeby.notification.domain.handler

import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationType
import com.closeby.notification.domain.repository.NotificationRepository
import com.closeby.notification.presentation.NotificationUnreadHolder
import java.util.UUID

/**
 * Helper for creating in-app notifications from trust/admin/ad flows.
 */
object AppNotificationPublisher {

    private val deduplicator = NotificationDeduplicator()

    suspend fun publish(
        repository: NotificationRepository,
        userId: String,
        type: NotificationType,
        title: String,
        body: String,
        referenceType: String? = null,
        referenceId: String? = null
    ) {
        val eventKey = NotificationDeduplicator.eventKey(userId, type, referenceType, referenceId)
        if (deduplicator.isDuplicate(eventKey)) return
        if (deduplicator.existsInRepository(repository, userId, type, referenceType, referenceId)) {
            return
        }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            body = body,
            referenceType = referenceType,
            referenceId = referenceId,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        repository.create(notification)
            .onSuccess {
                repository.getUnreadCount(userId)
                    .onSuccess { count -> NotificationUnreadHolder.update(count) }
            }
    }
}
