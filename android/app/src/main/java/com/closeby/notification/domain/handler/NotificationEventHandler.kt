package com.closeby.notification.domain.handler

import com.closeby.feature.provider.data.remote.ProviderManagementRemoteDataSource
import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationReferenceType
import com.closeby.notification.domain.model.NotificationType
import com.closeby.notification.domain.repository.NotificationRepository
import com.closeby.notification.presentation.NotificationUnreadHolder
import com.closeby.request.domain.notification.RequestNotificationBridge
import com.closeby.request.domain.notification.RequestNotificationEvent
import com.closeby.request.domain.repository.ServiceRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Subscribes to [RequestNotificationBridge] and persists in-app notifications.
 *
 * FCM push delivery is not configured — notifications are stored and shown in-app only.
 */
class NotificationEventHandler(
    private val notificationRepository: NotificationRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val resolveProviderUserId: suspend (providerId: String) -> String?
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var subscribed = false

    fun start() {
        if (subscribed) return
        subscribed = true
        RequestNotificationBridge.subscribe { event ->
            scope.launch { handle(event) }
        }
    }

    private suspend fun handle(event: RequestNotificationEvent) {
        val request = serviceRequestRepository.getRequestById(event.requestId).getOrNull() ?: return
        val (targetUserId, type, title) = when (event) {
            is RequestNotificationEvent.RequestAccepted -> Triple(
                request.customerId,
                NotificationType.REQUEST_ACCEPTED,
                "Request accepted"
            )
            is RequestNotificationEvent.RequestRejected -> Triple(
                request.customerId,
                NotificationType.REQUEST_REJECTED,
                "Request rejected"
            )
            is RequestNotificationEvent.RequestCompleted -> Triple(
                request.customerId,
                NotificationType.REQUEST_COMPLETED,
                "Request completed"
            )
            is RequestNotificationEvent.NewProviderRequest -> Triple(
                resolveProviderUserId(request.providerId),
                NotificationType.NEW_PROVIDER_REQUEST,
                "New service request"
            )
        }
        val userId = targetUserId?.takeIf { it.isNotBlank() } ?: return

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            body = event.message,
            referenceType = NotificationReferenceType.REQUEST,
            referenceId = event.requestId,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )

        notificationRepository.create(notification)
            .onSuccess { refreshUnreadCount(userId) }
    }

    private suspend fun refreshUnreadCount(userId: String) {
        notificationRepository.getUnreadCount(userId)
            .onSuccess { NotificationUnreadHolder.update(it) }
    }

    companion object {
        fun defaultProviderUserResolver(
            remote: ProviderManagementRemoteDataSource = ProviderManagementRemoteDataSource()
        ): suspend (String) -> String? = { providerId ->
            remote.getProviderById(providerId)?.userId
        }

        /** Mock fallback when provider rows have no linked auth user. */
        fun mockProviderUserResolver(demoUserId: String): suspend (String) -> String? =
            { _ -> demoUserId }
    }
}
