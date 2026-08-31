package com.closeby.notification

import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationReferenceType
import com.closeby.notification.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationDeepLinkTest {

    @Test
    fun requestNotificationReferencesRequestId() {
        val notification = AppNotification(
            id = "n1",
            userId = "u1",
            type = NotificationType.REQUEST_ACCEPTED,
            title = "Accepted",
            body = "Your request was accepted.",
            referenceType = NotificationReferenceType.REQUEST,
            referenceId = "req-42",
            isRead = false,
            createdAt = 0L
        )
        assertEquals(NotificationReferenceType.REQUEST, notification.referenceType)
        assertEquals("req-42", notification.referenceId)
    }

    @Test
    fun verificationNotificationUsesVerificationReference() {
        val notification = AppNotification(
            id = "n2",
            userId = "u1",
            type = NotificationType.VERIFICATION_APPROVED,
            title = "Verified",
            body = "Your provider profile was verified.",
            referenceType = NotificationReferenceType.VERIFICATION,
            referenceId = "prov-1",
            isRead = false,
            createdAt = 0L
        )
        assertEquals(NotificationReferenceType.VERIFICATION, notification.referenceType)
    }
}
