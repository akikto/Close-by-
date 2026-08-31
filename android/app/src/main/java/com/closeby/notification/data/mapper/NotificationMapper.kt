package com.closeby.notification.data.mapper

import com.closeby.notification.data.model.NotificationDto
import com.closeby.notification.data.model.NotificationInsertDto
import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationType
import java.time.Instant

object NotificationMapper {

    fun toDomain(dto: NotificationDto): AppNotification? = runCatching {
        AppNotification(
            id = dto.id,
            userId = dto.userId,
            type = NotificationType.valueOf(dto.type),
            title = dto.title,
            body = dto.body,
            referenceType = dto.referenceType,
            referenceId = dto.referenceId,
            isRead = dto.isRead,
            createdAt = Instant.parse(dto.createdAt).toEpochMilli()
        )
    }.getOrNull()

    fun toInsertDto(notification: AppNotification): NotificationInsertDto =
        NotificationInsertDto(
            userId = notification.userId,
            type = notification.type.name,
            title = notification.title,
            body = notification.body,
            referenceType = notification.referenceType,
            referenceId = notification.referenceId,
            isRead = notification.isRead
        )
}
