package com.closeby.request.domain.notification

/**
 * In-app notification seam for request lifecycle events.
 * Push delivery is out of scope for Phase 4; UI can observe [events].
 */
sealed class RequestNotificationEvent(val message: String) {
    data class RequestAccepted(val requestId: String) :
        RequestNotificationEvent("Your service request was accepted.")

    data class RequestRejected(val requestId: String) :
        RequestNotificationEvent("Your service request was rejected.")

    data class RequestCompleted(val requestId: String) :
        RequestNotificationEvent("Your request was completed.")

    data class NewProviderRequest(val requestId: String) :
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
