package com.closeby.notification

import com.closeby.notification.domain.handler.NotificationDeduplicator
import com.closeby.notification.domain.model.NotificationReferenceType
import com.closeby.notification.domain.model.NotificationType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDeduplicatorTest {

    private val deduplicator = NotificationDeduplicator()

    @Test
    fun duplicateEventKeyIsDetected() {
        val key = NotificationDeduplicator.eventKey(
            userId = "user-1",
            type = NotificationType.REQUEST_ACCEPTED,
            referenceType = NotificationReferenceType.REQUEST,
            referenceId = "req-1"
        )
        assertFalse(deduplicator.isDuplicate(key))
        assertTrue(deduplicator.isDuplicate(key))
    }
}
