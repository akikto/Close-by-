package com.closeby.request.domain.notification

/**
 * In-app notification seam for request lifecycle events.
 * Push delivery is out of scope for Phase 4; UI can observe [events].
 */
sealed class RequestNotificationEvent(val message: String) {
    abstract val requestId: String

    data class RequestAccepted(override val requestId: String) :
        RequestNotificationEvent("Your service request was accepted.")

    data class RequestRejected(override val requestId: String) :
        RequestNotificationEvent("Your service request was rejected.")

    data class RequestCompleted(override val requestId: String) :
        RequestNotificationEvent("Your request was completed.")

    data class NewProviderRequest(override val requestId: String) :
        RequestNotificationEvent("New service request received.")
}

object RequestNotificationBridge {
    private val listeners = mutableListOf<(RequestNotificationEvent) -> Unit>()

    fun publish(event: RequestNotificationEvent) {
        listeners.forEach { it(event) }
    }

    fun subscribe(listener: (RequestNotificationEvent) -> Unit) {
        listeners.add(listener)
    }
}
