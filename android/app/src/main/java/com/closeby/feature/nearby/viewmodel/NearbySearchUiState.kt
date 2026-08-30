package com.closeby.feature.nearby.viewmodel

import com.closeby.feature.nearby.domain.NearbyServiceResult

/** Everything the nearby-search screen needs to render, in one exhaustive sealed type. */
sealed class NearbySearchUiState {
    object Loading : NearbySearchUiState()
    data class Success(val results: List<NearbyServiceResult>) : NearbySearchUiState()
    object Empty : NearbySearchUiState()
    object PermissionDenied : NearbySearchUiState()
    object PermissionPermanentlyDenied : NearbySearchUiState()
    object GpsDisabled : NearbySearchUiState()
    object LocationUnavailable : NearbySearchUiState()
    object NetworkUnavailable : NearbySearchUiState()
    data class Error(val message: String) : NearbySearchUiState()
}
