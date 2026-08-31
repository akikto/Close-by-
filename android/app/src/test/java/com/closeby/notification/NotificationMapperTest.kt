package com.closeby.notification

import com.closeby.notification.data.mapper.NotificationMapper
import com.closeby.notification.data.model.NotificationDto
import com.closeby.notification.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NotificationMapperTest {

    @Test
    fun mapsDtoToDomain() {
        val dto = NotificationDto(
            id = "notif-1",
            userId = "user-1",
            type = NotificationType.REQUEST_COMPLETED.name,
            title = "Done",
            body = "Your request was completed.",
            referenceType = "REQUEST",
            referenceId = "req-1",
            isRead = false,
            createdAt = "2026-01-15T10:30:00Z"
        )

        val domain = NotificationMapper.toDomain(dto)

        assertNotNull(domain)
        assertEquals(NotificationType.REQUEST_COMPLETED, domain?.type)
        assertEquals("req-1", domain?.referenceId)
    }
}
