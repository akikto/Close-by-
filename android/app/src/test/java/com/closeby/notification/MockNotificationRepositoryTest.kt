package com.closeby.notification

import com.closeby.notification.data.repository.MockNotificationRepository
import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationReferenceType
import com.closeby.notification.domain.model.NotificationType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MockNotificationRepositoryTest {

    private lateinit var repository: MockNotificationRepository
    private val userId = "user-test-001"

    @Before
    fun setUp() {
        repository = MockNotificationRepository()
    }

    @Test
    fun createAndListNotifications() = runTest {
        val created = repository.create(sampleNotification()).getOrThrow()
        val list = repository.getNotifications(userId).getOrThrow()
        assertEquals(1, list.size)
        assertEquals(created.id, list.first().id)
    }

    @Test
    fun unreadCountReflectsReadState() = runTest {
        val first = repository.create(sampleNotification(isRead = false)).getOrThrow()
        repository.create(sampleNotification(isRead = true)).getOrThrow()
        assertEquals(1, repository.getUnreadCount(userId).getOrThrow())

        repository.markRead(first.id).getOrThrow()
        assertEquals(0, repository.getUnreadCount(userId).getOrThrow())
    }

    @Test
    fun markAllReadClearsUnreadCount() = runTest {
        repository.create(sampleNotification(isRead = false)).getOrThrow()
        repository.create(sampleNotification(isRead = false)).getOrThrow()
        assertEquals(2, repository.getUnreadCount(userId).getOrThrow())

        repository.markAllRead(userId).getOrThrow()
        assertEquals(0, repository.getUnreadCount(userId).getOrThrow())
        val unread = repository.getNotifications(userId).getOrThrow().count { !it.isRead }
        assertEquals(0, unread)
    }

    @Test
    fun markReadFailsForUnknownId() = runTest {
        val result = repository.markRead("missing-id")
        assertTrue(result.isFailure)
    }

    private fun sampleNotification(isRead: Boolean = false): AppNotification =
        AppNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = NotificationType.REQUEST_ACCEPTED,
            title = "Request accepted",
            body = "Your service request was accepted.",
            referenceType = NotificationReferenceType.REQUEST,
            referenceId = "req_001",
            isRead = isRead,
            createdAt = System.currentTimeMillis()
        )
}
