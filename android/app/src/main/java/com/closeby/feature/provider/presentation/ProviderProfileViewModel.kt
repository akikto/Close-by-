package com.closeby.feature.provider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.data.location.LocationSession
import com.closeby.availability.domain.repository.AvailabilityRepository
import com.closeby.feature.provider.domain.model.ProviderProfile
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProviderProfileViewModel(
    private val providerId: String,
    private val currentProviderId: String?,
    private val repository: ProviderManagementRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val locationSession: LocationSession?
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProviderProfile>>(UiState.Idle)
    val uiState: StateFlow<UiState<ProviderProfile>> = _uiState.asStateFlow()

    init {
        locationSession?.bind(viewModelScope)
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val coords = locationSession?.coordinates?.value
            val isOwn = currentProviderId == providerId
            repository.getProviderProfile(
                providerId = providerId,
                viewerLatitude = coords?.latitude,
                viewerLongitude = coords?.longitude,
                isOwnProfile = isOwn
            )
                .onSuccess { profile ->
                    val availability = availabilityRepository.getProviderAvailability(providerId)
                        .getOrElse { emptyList() }
                    _uiState.value = UiState.Success(profile.copy(availability = availability))
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load profile.")
                }
        }
    }
}
