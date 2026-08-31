package com.closeby.trust.domain.model

enum class VerificationStatus {
    NOT_SUBMITTED,
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED;

    val isVerified: Boolean get() = this == APPROVED

    companion object {
        fun fromRaw(raw: String?): VerificationStatus =
            entries.firstOrNull { it.name == raw?.trim()?.uppercase() } ?: NOT_SUBMITTED
    }
}

data class VerificationSubmission(
    val id: String,
    val providerId: String,
    val businessName: String,
    val contactPhone: String,
    val description: String?,
    val documentUrl: String?,
    val status: VerificationStatus,
    val adminNote: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class VerificationInput(
    val businessName: String,
    val contactPhone: String,
    val description: String?,
    val documentUrl: String?
)
