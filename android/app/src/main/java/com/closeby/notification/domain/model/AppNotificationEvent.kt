package com.closeby.notification.domain.model

/**
 * Cross-cutting in-app notification events for trust, ads, and account flows.
 */
sealed class AppNotificationEvent {
    abstract val userId: String
    abstract val type: NotificationType
    abstract val title: String
    abstract val body: String
    abstract val referenceType: String?
    abstract val referenceId: String?
    abstract val eventKey: String

    data class Trust(
        override val userId: String,
        override val type: NotificationType,
        override val title: String,
        override val body: String,
        override val referenceType: String?,
        override val referenceId: String?,
        override val eventKey: String
    ) : AppNotificationEvent()

    data class Advertisement(
        override val userId: String,
        override val type: NotificationType,
        override val title: String,
        override val body: String,
        override val referenceType: String?,
        override val referenceId: String?,
        override val eventKey: String
    ) : AppNotificationEvent()

    data class Account(
        override val userId: String,
        override val type: NotificationType,
        override val title: String,
        override val body: String,
        override val referenceType: String?,
        override val referenceId: String?,
        override val eventKey: String
    ) : AppNotificationEvent()
}

object AppNotificationEventBridge {
    private val listeners = mutableListOf<(AppNotificationEvent) -> Unit>()

    fun publish(event: AppNotificationEvent) {
        listeners.forEach { it(event) }
    }

    fun subscribe(listener: (AppNotificationEvent) -> Unit) {
        listeners.add(listener)
    }
}
