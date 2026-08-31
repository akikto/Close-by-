package com.closeby.notification.domain.model

enum class NotificationType {
    REQUEST_ACCEPTED,
    REQUEST_REJECTED,
    REQUEST_COMPLETED,
    REQUEST_CANCELLED,
    NEW_PROVIDER_REQUEST,
    REVIEW_RECEIVED,
    REVIEW_PUBLISHED,
    VERIFICATION_SUBMITTED,
    VERIFICATION_APPROVED,
    VERIFICATION_REJECTED,
    VERIFICATION_SUSPENDED,
    NEW_VERIFICATION,
    NEW_REPORT,
    REPORT_STATUS_UPDATED,
    NEW_AD_REQUEST,
    AD_SUBMITTED,
    AD_APPROVED,
    AD_REJECTED,
    AD_PAUSED,
    AD_RESUMED,
    ACCOUNT_DELETION_REQUESTED,
    ACCOUNT_SECURITY_EVENT
}

object NotificationReferenceType {
    const val REQUEST = "REQUEST"
    const val REVIEW = "REVIEW"
    const val VERIFICATION = "VERIFICATION"
    const val REPORT = "REPORT"
    const val AD = "AD"
    const val ACCOUNT = "ACCOUNT"
    const val ADMIN = "ADMIN"
}

data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val referenceType: String?,
    val referenceId: String?,
    val isRead: Boolean,
    val createdAt: Long
)
