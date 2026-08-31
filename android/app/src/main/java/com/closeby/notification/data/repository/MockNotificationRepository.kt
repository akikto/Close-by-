package com.closeby.notification.data.repository

import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationReferenceType
import com.closeby.notification.domain.model.NotificationType
import com.closeby.notification.domain.repository.NotificationRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory notification store for local development without Supabase.
 */
class MockNotificationRepository : NotificationRepository {

    private val notifications = CopyOnWriteArrayList<AppNotification>()
    private val byUser = ConcurrentHashMap<String, MutableSet<String>>()

    override suspend fun getNotifications(userId: String): Result<List<AppNotification>> =
        Result.success(
            notifications
                .filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
        )

    override suspend fun getUnreadCount(userId: String): Result<Int> =
        Result.success(notifications.count { it.userId == userId && !it.isRead })

    override suspend fun markRead(id: String): Result<Unit> = runCatching {
        val index = notifications.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException("Notification not found.")
        val current = notifications[index]
        notifications[index] = current.copy(isRead = true)
    }

    override suspend fun markAllRead(userId: String): Result<Unit> = runCatching {
        for (i in notifications.indices) {
            val item = notifications[i]
            if (item.userId == userId && !item.isRead) {
                notifications[i] = item.copy(isRead = true)
            }
        }
    }

    override suspend fun create(notification: AppNotification): Result<AppNotification> =
        runCatching {
            val created = if (notification.id.isBlank()) {
                notification.copy(
                    id = UUID.randomUUID().toString(),
                    createdAt = if (notification.createdAt > 0L) notification.createdAt
                    else System.currentTimeMillis()
                )
            } else {
                notification
            }
            notifications.add(created)
            byUser.computeIfAbsent(created.userId) { mutableSetOf() }.add(created.id)
            created
        }

    fun seedSample(userId: String) {
        if (notifications.any { it.userId == userId }) return
        val now = System.currentTimeMillis()
        listOf(
            AppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = NotificationType.REQUEST_ACCEPTED,
                title = "Request accepted",
                body = "Your service request was accepted.",
                referenceType = NotificationReferenceType.REQUEST,
                referenceId = "req_002",
                isRead = false,
                createdAt = now - 3_600_000
            ),
            AppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = NotificationType.NEW_PROVIDER_REQUEST,
                title = "New request",
                body = "New service request received.",
                referenceType = NotificationReferenceType.REQUEST,
                referenceId = "req_001",
                isRead = true,
                createdAt = now - 86_400_000
            )
        ).forEach { notifications.add(it) }
    }
}
