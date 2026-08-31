package com.closeby.feature.servicelisting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.servicelisting.domain.repository.LocationProvider
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import com.closeby.feature.servicelisting.presentation.model.ServiceDetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Service Details screen foundation.
 *
 * Only prepares data + exposes callbacks for "Call Provider" / "SMS Provider"
 * and "View Provider Profile" navigation. The actual phone/SMS intents and
 * the Provider Profile screen are NOT implemented here — those are wired up
 * by the Base Project / Provider module using the callback contracts below.
 */
class ServiceDetailsViewModel(
    private val serviceId: String,
    private val serviceRepository: ServiceRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceDetailsUiState>(ServiceDetailsUiState.Loading)
    val uiState: StateFlow<ServiceDetailsUiState> = _uiState.asStateFlow()

    init {
        locationProvider.start(viewModelScope)
        load()
    }

    fun load() {
        _uiState.value = ServiceDetailsUiState.Loading
        viewModelScope.launch {
            serviceRepository.getServiceById(serviceId)
                .map { listing -> locationProvider.attachDistances(listOf(listing)).first() }
                .onSuccess { listing -> _uiState.value = ServiceDetailsUiState.Success(listing) }
                .onFailure { throwable ->
                    _uiState.value = ServiceDetailsUiState.Error(
                        message = throwable.message ?: "Unable to load this service.",
                        isRetryable = true
                    )
                }
        }
    }

    fun retry() = load()
}

/**
 * Callback contract for actions on the Service Details screen.
 * The Base Project supplies real implementations that launch the native
 * Dialer / SMS apps and navigate to the Provider Profile screen.
 * This module never implements calling, SMS, or chat itself.
 */
interface ServiceDetailsActions {
    fun onCallProvider(phoneNumber: String)
    fun onSmsProvider(phoneNumber: String)
    fun onViewProviderProfile(providerId: String)
    fun onRequestService(serviceId: String, providerId: String, serviceTitle: String, providerName: String, providerPhone: String)
    fun onReportService(serviceId: String)
    fun onBack()
}
