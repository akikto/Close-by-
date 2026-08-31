package com.closeby.notification.domain.handler

import com.closeby.notification.domain.model.AppNotificationEvent
import com.closeby.notification.domain.model.AppNotificationEventBridge
import com.closeby.notification.domain.model.NotificationReferenceType
import com.closeby.notification.domain.model.NotificationType

/**
 * Publishes in-app notification events to [AppNotificationEventBridge].
 * Used by trust, admin, and advertisement flows — not a second notification system.
 */
object NotificationEventPublisher {

    fun verificationSubmitted(userId: String, providerId: String) {
        publish(
            userId = userId,
            type = NotificationType.VERIFICATION_SUBMITTED,
            title = "Verification submitted",
            body = "Your verification request was received and is pending review.",
            referenceType = NotificationReferenceType.VERIFICATION,
            referenceId = providerId,
            eventKey = "verification_submitted:$userId:$providerId"
        )
    }

    fun verificationApproved(userId: String, providerId: String) {
        publish(
            userId = userId,
            type = NotificationType.VERIFICATION_APPROVED,
            title = "Verification approved",
            body = "Your provider profile is now verified.",
            referenceType = NotificationReferenceType.VERIFICATION,
            referenceId = providerId,
            eventKey = "verification_approved:$userId:$providerId"
        )
    }

    fun verificationRejected(userId: String, providerId: String, reason: String?) {
        publish(
            userId = userId,
            type = NotificationType.VERIFICATION_REJECTED,
            title = "Verification rejected",
            body = reason?.takeIf { it.isNotBlank() } ?: "Your verification request was not approved.",
            referenceType = NotificationReferenceType.VERIFICATION,
            referenceId = providerId,
            eventKey = "verification_rejected:$userId:$providerId"
        )
    }

    fun verificationSuspended(userId: String, providerId: String) {
        publish(
            userId = userId,
            type = NotificationType.VERIFICATION_SUSPENDED,
            title = "Verification suspended",
            body = "Your verification status was suspended. Contact support if you need help.",
            referenceType = NotificationReferenceType.VERIFICATION,
            referenceId = providerId,
            eventKey = "verification_suspended:$userId:$providerId"
        )
    }

    fun reviewReceived(userId: String, requestId: String, providerId: String) {
        publish(
            userId = userId,
            type = NotificationType.REVIEW_RECEIVED,
            title = "New review received",
            body = "Someone left a review on your completed request.",
            referenceType = NotificationReferenceType.REVIEW,
            referenceId = requestId,
            eventKey = "review_received:$userId:$requestId"
        )
    }

    fun reportStatusUpdated(userId: String, reportId: String, status: String) {
        publish(
            userId = userId,
            type = NotificationType.REPORT_STATUS_UPDATED,
            title = "Report update",
            body = "Your report status is now: $status",
            referenceType = NotificationReferenceType.REPORT,
            referenceId = reportId,
            eventKey = "report_status:$userId:$reportId:$status"
        )
    }

    fun adSubmitted(userId: String, adId: String) {
        publish(
            userId = userId,
            type = NotificationType.AD_SUBMITTED,
            title = "Advertisement submitted",
            body = "Your advertisement is pending admin approval.",
            referenceType = NotificationReferenceType.AD,
            referenceId = adId,
            eventKey = "ad_submitted:$userId:$adId"
        )
    }

    fun adApproved(userId: String, adId: String) {
        publish(
            userId = userId,
            type = NotificationType.AD_APPROVED,
            title = "Advertisement approved",
            body = "Your advertisement is now active.",
            referenceType = NotificationReferenceType.AD,
            referenceId = adId,
            eventKey = "ad_approved:$userId:$adId"
        )
    }

    fun adRejected(userId: String, adId: String, reason: String?) {
        publish(
            userId = userId,
            type = NotificationType.AD_REJECTED,
            title = "Advertisement rejected",
            body = reason?.takeIf { it.isNotBlank() } ?: "Your advertisement was not approved.",
            referenceType = NotificationReferenceType.AD,
            referenceId = adId,
            eventKey = "ad_rejected:$userId:$adId"
        )
    }

    fun adPaused(userId: String, adId: String) {
        publish(
            userId = userId,
            type = NotificationType.AD_PAUSED,
            title = "Advertisement paused",
            body = "Your advertisement has been paused.",
            referenceType = NotificationReferenceType.AD,
            referenceId = adId,
            eventKey = "ad_paused:$userId:$adId"
        )
    }

    fun adResumed(userId: String, adId: String) {
        publish(
            userId = userId,
            type = NotificationType.AD_RESUMED,
            title = "Advertisement resumed",
            body = "Your advertisement is active again.",
            referenceType = NotificationReferenceType.AD,
            referenceId = adId,
            eventKey = "ad_resumed:$userId:$adId"
        )
    }

    fun accountDeletionRequested(userId: String) {
        publish(
            userId = userId,
            type = NotificationType.ACCOUNT_DELETION_REQUESTED,
            title = "Account deletion requested",
            body = "We received your account deletion request.",
            referenceType = NotificationReferenceType.ACCOUNT,
            referenceId = userId,
            eventKey = "account_deletion:$userId"
        )
    }

    fun accountSecurityEvent(userId: String, message: String) {
        publish(
            userId = userId,
            type = NotificationType.ACCOUNT_SECURITY_EVENT,
            title = "Account security",
            body = message,
            referenceType = NotificationReferenceType.ACCOUNT,
            referenceId = userId,
            eventKey = "account_security:$userId:${message.hashCode()}"
        )
    }

    private fun publish(
        userId: String,
        type: NotificationType,
        title: String,
        body: String,
        referenceType: String?,
        referenceId: String?,
        eventKey: String
    ) {
        AppNotificationEventBridge.publish(
            AppNotificationEvent.Trust(
                userId = userId,
                type = type,
                title = title,
                body = body,
                referenceType = referenceType,
                referenceId = referenceId,
                eventKey = eventKey
            )
        )
    }
}
