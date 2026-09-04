package com.closeby.notification.domain.handler

import com.closeby.app.BuildConfig
import com.closeby.feature.provider.data.remote.ProviderManagementRemoteDataSource
import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.AppNotificationEvent
import com.closeby.notification.domain.model.AppNotificationEventBridge
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
 * Subscribes to request and app notification bridges and persists in-app notifications.
 *
 * FCM push delivery is optional via [PushNotificationGateway]; in-app storage is primary.
 */
class NotificationEventHandler(
    private val notificationRepository: NotificationRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val resolveProviderUserId: suspend (providerId: String) -> String?,
    private val deduplicator: NotificationDeduplicator = NotificationDeduplicator()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var subscribed = false

    fun start() {
        if (subscribed) return
        subscribed = true
        RequestNotificationBridge.subscribe { event ->
            scope.launch { handleRequestEvent(event) }
        }
        AppNotificationEventBridge.subscribe { event ->
            scope.launch { handleAppEvent(event) }
        }
    }

    private suspend fun handleRequestEvent(event: RequestNotificationEvent) {
        // Request lifecycle notifications are created server-side (schema_phase18 triggers)
        // when Supabase is configured. Demo/mock mode keeps in-process delivery.
        if (BuildConfig.SUPABASE_URL.isNotBlank()) return

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
            is RequestNotificationEvent.RequestCancelled -> Triple(
                resolveProviderUserId(request.providerId),
                NotificationType.REQUEST_CANCELLED,
                "Request cancelled"
            )
            is RequestNotificationEvent.NewProviderRequest -> Triple(
                resolveProviderUserId(request.providerId),
                NotificationType.NEW_PROVIDER_REQUEST,
                "New service request"
            )
        }
        val userId = targetUserId?.takeIf { it.isNotBlank() } ?: return
        val eventKey = NotificationDeduplicator.eventKey(
            userId, type, NotificationReferenceType.REQUEST, event.requestId
        )
        persist(
            userId = userId,
            type = type,
            title = title,
            body = event.message,
            referenceType = NotificationReferenceType.REQUEST,
            referenceId = event.requestId,
            eventKey = eventKey
        )
    }

    private suspend fun handleAppEvent(event: AppNotificationEvent) {
        persist(
            userId = event.userId,
            type = event.type,
            title = event.title,
            body = event.body,
            referenceType = event.referenceType,
            referenceId = event.referenceId,
            eventKey = event.eventKey
        )
    }

    private suspend fun persist(
        userId: String,
        type: NotificationType,
        title: String,
        body: String,
        referenceType: String?,
        referenceId: String?,
        eventKey: String
    ) {
        if (deduplicator.isDuplicate(eventKey)) return
        if (deduplicator.existsInRepository(notificationRepository, userId, type, referenceType, referenceId)) {
            return
        }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            body = body,
            referenceType = referenceType,
            referenceId = referenceId,
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

        fun mockProviderUserResolver(demoUserId: String): suspend (String) -> String? =
            { _ -> demoUserId }
    }
}
