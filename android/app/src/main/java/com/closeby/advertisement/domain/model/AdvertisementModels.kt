package com.closeby.advertisement.domain.model

enum class AdStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PAUSED,
    EXPIRED
}

enum class AdRadiusPreset(val meters: Int) {
    KM_5(5_000),
    KM_10(10_000),
    KM_30(30_000),
    KM_50(50_000),
    CUSTOM(-1);

    companion object {
        fun fromMeters(meters: Int): AdRadiusPreset =
            entries.firstOrNull { it != CUSTOM && it.meters == meters } ?: CUSTOM
    }
}

data class Advertisement(
    val id: String,
    val ownerId: String,
    val businessName: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val contactNumber: String,
    val latitude: Double,
    val longitude: Double,
    val targetRadiusMeters: Int,
    val startAt: Long,
    val endAt: Long,
    val status: AdStatus,
    val approvedBy: String?,
    val approvedAt: Long?,
    val rejectionReason: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class AdvertisementInput(
    val businessName: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val contactNumber: String,
    val latitude: Double,
    val longitude: Double,
    val targetRadiusMeters: Int,
    val startAt: Long,
    val endAt: Long
)
