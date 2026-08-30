package com.closeby.request.domain.model

/**
 * Status of a [ServiceRequest].
 *
 * IMPORTANT: ACCEPTED never means "payment completed" — Close by has no
 * payment state. There is no PAID / PAYMENT_PENDING / PAYMENT_SUCCESS /
 * TRANSACTION_ID anywhere in this model.
 */
enum class ServiceRequestStatus {
    /** Request sent and waiting for the provider to respond. */
    PENDING,

    /** Provider agreed to provide the service (not a payment confirmation). */
    ACCEPTED,

    /** Provider declined. */
    REJECTED,

    /** Service was completed. */
    COMPLETED,

    /** Request was cancelled. */
    CANCELLED;

    /** Whether transitioning from this status to [target] is a valid domain transition. */
    fun canTransitionTo(target: ServiceRequestStatus): Boolean =
        VALID_TRANSITIONS[this]?.contains(target) == true

    companion object {
        private val VALID_TRANSITIONS: Map<ServiceRequestStatus, Set<ServiceRequestStatus>> = mapOf(
            PENDING to setOf(ACCEPTED, REJECTED, CANCELLED),
            ACCEPTED to setOf(COMPLETED, CANCELLED),
            REJECTED to emptySet(),
            COMPLETED to emptySet(),
            CANCELLED to emptySet()
        )
    }
}
