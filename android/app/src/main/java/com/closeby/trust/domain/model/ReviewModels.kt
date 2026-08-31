package com.closeby.trust.domain.model

enum class ReviewerRole { CUSTOMER, PROVIDER }

enum class ModerationStatus { VISIBLE, HIDDEN, PENDING }

data class Review(
    val id: String,
    val requestId: String,
    val serviceId: String,
    val providerId: String,
    val customerId: String?,
    val reviewerId: String,
    val revieweeId: String,
    val reviewerRole: ReviewerRole,
    val overallRating: Int,
    val serviceQuality: Int?,
    val behaviour: Int?,
    val reliability: Int?,
    val professionalism: Int?,
    val comment: String?,
    val moderationStatus: ModerationStatus = ModerationStatus.VISIBLE,
    val isVisible: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

data class ReviewInput(
    val requestId: String,
    val overallRating: Int,
    val serviceQuality: Int? = null,
    val behaviour: Int? = null,
    val reliability: Int? = null,
    val professionalism: Int? = null,
    val comment: String? = null
)

data class RatingSummary(
    val averageRating: Double,
    val reviewCount: Int
)
