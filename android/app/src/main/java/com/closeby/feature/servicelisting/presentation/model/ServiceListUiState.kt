package com.closeby.feature.servicelisting.presentation.model

import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.SortOption

/**
 * UI state for the Service Listing screen. Covers every required state:
 * loading, success, empty (several reasons), and error/retry.
 */
sealed interface ServiceListUiState {

    data object Loading : ServiceListUiState

    data class Success(
        val listings: List<ServiceListing>,
        val query: String = "",
        val filter: ServiceFilter = ServiceFilter(),
        val sortOption: SortOption = SortOption.DEFAULT,
        val totalCount: Int = listings.size,
        val hasMore: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : ServiceListUiState

    data class Empty(
        val reason: EmptyReason,
        val query: String = "",
        val filter: ServiceFilter = ServiceFilter()
    ) : ServiceListUiState

    data class Error(
        val message: String,
        val isRetryable: Boolean = true
    ) : ServiceListUiState
}

enum class EmptyReason {
    NO_SERVICES_IN_RADIUS,
    NO_SERVICES_IN_CATEGORY,
    NO_SEARCH_RESULTS,
    NO_SERVICES_AVAILABLE
}

/** UI state for the Service Details screen. */
sealed interface ServiceDetailsUiState {
    data object Loading : ServiceDetailsUiState
    data class Success(val listing: ServiceListing) : ServiceDetailsUiState
    data class Error(val message: String, val isRetryable: Boolean = true) : ServiceDetailsUiState
}
