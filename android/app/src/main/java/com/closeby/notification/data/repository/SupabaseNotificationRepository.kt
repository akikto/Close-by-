package com.closeby.notification.data.repository

import com.closeby.notification.data.mapper.NotificationMapper
import com.closeby.notification.data.remote.NotificationRemoteDataSource
import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.repository.NotificationRepository

class SupabaseNotificationRepository(
    private val remote: NotificationRemoteDataSource = NotificationRemoteDataSource()
) : NotificationRepository {

    override suspend fun getNotifications(userId: String): Result<List<AppNotification>> =
        runCatching {
            remote.getByUser(userId).mapNotNull(NotificationMapper::toDomain)
        }

    override suspend fun getUnreadCount(userId: String): Result<Int> =
        runCatching { remote.countUnread(userId) }

    override suspend fun markRead(id: String): Result<Unit> =
        runCatching { remote.markRead(id) }

    override suspend fun markAllRead(userId: String): Result<Unit> =
        runCatching { remote.markAllRead(userId) }

    override suspend fun create(notification: AppNotification): Result<AppNotification> =
        runCatching {
            val dto = remote.insert(NotificationMapper.toInsertDto(notification))
            NotificationMapper.toDomain(dto)
                ?: throw IllegalStateException("Created notification has invalid data.")
        }
}
