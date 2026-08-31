package com.closeby.notification.domain.repository

import com.closeby.notification.domain.model.AppNotification

interface NotificationRepository {

    suspend fun getNotifications(userId: String): Result<List<AppNotification>>

    suspend fun getUnreadCount(userId: String): Result<Int>

    suspend fun markRead(id: String): Result<Unit>

    suspend fun markAllRead(userId: String): Result<Unit>

    /** Internal use — persists a notification (e.g. from [NotificationEventHandler]). */
    suspend fun create(notification: AppNotification): Result<AppNotification>
}
