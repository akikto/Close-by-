package com.closeby.notification.domain.model

enum class NotificationType {
    REQUEST_ACCEPTED,
    REQUEST_REJECTED,
    REQUEST_COMPLETED,
    NEW_PROVIDER_REQUEST,
    REVIEW_RECEIVED,
    REVIEW_PUBLISHED,
    VERIFICATION_APPROVED,
    VERIFICATION_REJECTED,
    VERIFICATION_SUSPENDED,
    NEW_VERIFICATION,
    NEW_REPORT,
    NEW_AD_REQUEST,
    AD_APPROVED,
    AD_REJECTED
}

object NotificationReferenceType {
    const val REQUEST = "REQUEST"
    const val REVIEW = "REVIEW"
    const val VERIFICATION = "VERIFICATION"
    const val REPORT = "REPORT"
    const val AD = "AD"
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
