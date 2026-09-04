package com.closeby.feature.provider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.domain.auth.AuthRepository
import com.closeby.feature.provider.domain.model.ProviderOnboardingInput
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BecomeProviderUiState {
    data object Idle : BecomeProviderUiState
    data object Saving : BecomeProviderUiState
    data class Ready(
        val name: String,
        val category: ServiceCategory,
        val phoneNumber: String,
        val latitude: Double?,
        val longitude: Double?,
        val locationCaptured: Boolean
    ) : BecomeProviderUiState
    data class Success(val providerId: String) : BecomeProviderUiState
    data class Error(val message: String) : BecomeProviderUiState
}

class BecomeProviderViewModel(
    private val authRepository: AuthRepository,
    private val providerRepository: ProviderManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BecomeProviderUiState>(
        BecomeProviderUiState.Ready(
            name = "",
            category = ServiceCategory.VEHICLES,
            phoneNumber = "",
            latitude = null,
            longitude = null,
            locationCaptured = false
        )
    )
    val uiState: StateFlow<BecomeProviderUiState> = _uiState.asStateFlow()

    fun updateForm(transform: (BecomeProviderUiState.Ready) -> BecomeProviderUiState.Ready) {
        val current = _uiState.value
        if (current is BecomeProviderUiState.Ready) {
            _uiState.value = transform(current)
        }
    }

    fun setLocation(latitude: Double, longitude: Double) {
        updateForm { ready ->
            ready.copy(latitude = latitude, longitude = longitude, locationCaptured = true)
        }
    }

    fun setError(message: String) {
        _uiState.value = BecomeProviderUiState.Error(message)
    }

    fun resetErrorToReady() {
        _uiState.value = BecomeProviderUiState.Ready(
            name = "",
            category = ServiceCategory.VEHICLES,
            phoneNumber = "",
            latitude = null,
            longitude = null,
            locationCaptured = false
        )
    }

    fun submit() {
        val ready = _uiState.value as? BecomeProviderUiState.Ready ?: return
        viewModelScope.launch {
            val session = authRepository.getCurrentSession()
            if (session == null) {
                _uiState.value = BecomeProviderUiState.Error("Sign in before becoming a provider.")
                return@launch
            }
            if (ready.name.isBlank()) {
                _uiState.value = BecomeProviderUiState.Error("Enter your business or display name.")
                return@launch
            }
            if (ready.phoneNumber.isBlank()) {
                _uiState.value = BecomeProviderUiState.Error("Enter a contact phone number.")
                return@launch
            }
            val lat = ready.latitude
            val lng = ready.longitude
            if (lat == null || lng == null) {
                _uiState.value = BecomeProviderUiState.Error("Capture your service location using GPS.")
                return@launch
            }
            _uiState.value = BecomeProviderUiState.Saving
            providerRepository.createProviderProfile(
                userId = session.userId,
                input = ProviderOnboardingInput(
                    name = ready.name,
                    category = ready.category,
                    phoneNumber = ready.phoneNumber,
                    latitude = lat,
                    longitude = lng
                )
            ).onSuccess { providerId ->
                _uiState.value = BecomeProviderUiState.Success(providerId)
            }.onFailure { error ->
                _uiState.value = BecomeProviderUiState.Error(
                    error.message ?: "Could not create provider profile."
                )
            }
        }
    }
}
