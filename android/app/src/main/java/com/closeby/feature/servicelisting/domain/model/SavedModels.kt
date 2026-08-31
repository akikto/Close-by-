package com.closeby.feature.servicelisting.domain.model

data class SavedServiceEntry(
    val serviceId: String,
    val savedAt: Long
)

data class RecentlyViewedEntry(
    val serviceId: String,
    val viewedAt: Long
)
