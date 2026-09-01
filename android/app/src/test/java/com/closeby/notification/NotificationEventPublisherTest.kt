package com.closeby.notification

import com.closeby.notification.domain.handler.NotificationEventPublisher
import com.closeby.notification.domain.model.AppNotificationEventBridge
import com.closeby.notification.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationEventPublisherTest {

    @Test
    fun publishesVerificationApprovedEvent() {
        var captured: com.closeby.notification.domain.model.AppNotificationEvent? = null
        AppNotificationEventBridge.subscribe { captured = it }

        NotificationEventPublisher.verificationApproved("user-1", "prov-1")

        val event = captured as? com.closeby.notification.domain.model.AppNotificationEvent.Trust
        assertEquals(NotificationType.VERIFICATION_APPROVED, event?.type)
        assertEquals("user-1", event?.userId)
        assertEquals("prov-1", event?.referenceId)
    }
}
