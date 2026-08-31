package com.closeby.request.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.core.error.AppErrorMapper
import com.closeby.app.core.network.NetworkMonitor
import com.closeby.app.core.network.NetworkStatus
import com.closeby.availability.domain.repository.AvailabilityRepository
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import com.closeby.request.domain.model.BudgetUnit
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.request.domain.validation.RequestAvailabilityChecker
import com.closeby.request.domain.validation.ServiceRequestValidator
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

sealed class CreateRequestFormState {
    data object Idle : CreateRequestFormState()
    data object Submitting : CreateRequestFormState()
    data class Submitted(val request: ServiceRequest) : CreateRequestFormState()
    data class ValidationError(val message: String) : CreateRequestFormState()
    data class Error(val message: String) : CreateRequestFormState()
}

class CreateServiceRequestViewModel(
    private val serviceId: String,
    private val providerId: String,
    private val serviceTitle: String,
    private val providerName: String,
    private val providerPhone: String,
    private val customerId: String?,
    private val repository: ServiceRequestRepository,
    private val serviceRepository: ServiceRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val clientSessionStorage: ClientSessionStorage,
    private val networkMonitor: NetworkMonitor? = null
) : ViewModel() {

    private val _formState = MutableStateFlow<CreateRequestFormState>(CreateRequestFormState.Idle)
    val formState: StateFlow<CreateRequestFormState> = _formState.asStateFlow()

    private val _serviceLoadState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val serviceLoadState: StateFlow<UiState<Unit>> = _serviceLoadState.asStateFlow()

    private var isSubmitting = false

    init {
        verifyServiceActive()
    }

    private fun verifyServiceActive() {
        viewModelScope.launch {
            serviceRepository.getServiceById(serviceId)
                .onSuccess { _serviceLoadState.value = UiState.Success(Unit) }
                .onFailure { error ->
                    _serviceLoadState.value = UiState.Error(
                        error.message ?: "This service is not available."
                    )
                }
        }
    }

    fun sendRequest(
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        duration: String,
        budgetAmount: Double?,
        budgetUnit: BudgetUnit?,
        note: String?,
        customerName: String?,
        customerPhone: String?
    ) {
        if (isSubmitting) return

        if (networkMonitor?.status?.value == NetworkStatus.OFFLINE) {
            _formState.value = CreateRequestFormState.Error(AppErrorMapper.offlineRequestMessage())
            return
        }

        val validation = ServiceRequestValidator.validateNewRequest(
            serviceTitle = serviceTitle,
            date = date,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            budgetAmount = budgetAmount,
            note = note
        )
        validation.onFailure { error ->
            _formState.value = CreateRequestFormState.ValidationError(
                error.message ?: "Invalid request."
            )
            return
        }

        if (customerId == null) {
            ServiceRequestValidator.validateAnonymousContact(customerName, customerPhone)
                .onFailure { error ->
                    _formState.value = CreateRequestFormState.ValidationError(
                        error.message ?: "Contact details required."
                    )
                    return
                }
        }

        isSubmitting = true
        _formState.value = CreateRequestFormState.Submitting

        viewModelScope.launch {
            val sessionId = clientSessionStorage.getOrCreateSessionId()

            serviceRepository.getServiceById(serviceId)
                .onFailure {
                    isSubmitting = false
                    _formState.value = CreateRequestFormState.Error(
                        it.message ?: "Service is not available."
                    )
                    return@launch
                }

            RequestAvailabilityChecker.validateProviderAvailable(
                availabilityRepository,
                providerId,
                date,
                startTime,
                endTime
            ).onFailure { error ->
                isSubmitting = false
                _formState.value = CreateRequestFormState.ValidationError(
                    error.message ?: "Provider unavailable."
                )
                return@launch
            }

            val now = System.currentTimeMillis()
            val request = ServiceRequest(
                id = UUID.randomUUID().toString(),
                serviceId = serviceId,
                providerId = providerId,
                customerId = customerId,
                customerName = customerName?.trim(),
                customerPhone = customerPhone?.trim(),
                serviceTitle = serviceTitle,
                requestedDate = date,
                startTime = startTime,
                endTime = endTime,
                duration = duration.trim(),
                budgetAmount = budgetAmount,
                budgetUnit = budgetUnit,
                note = note?.trim(),
                clientSessionId = sessionId,
                providerName = providerName,
                providerPhone = providerPhone,
                status = ServiceRequestStatus.PENDING,
                createdAt = now,
                updatedAt = now
            )

            repository.createRequest(request)
                .onSuccess { created ->
                    clientSessionStorage.rememberRequestId(created.id)
                    isSubmitting = false
                    _formState.value = CreateRequestFormState.Submitted(created)
                }
                .onFailure { error ->
                    isSubmitting = false
                    _formState.value = CreateRequestFormState.Error(
                        AppErrorMapper.toUserMessage(error)
                    )
                }
        }
    }

    fun resetForm() {
        isSubmitting = false
        _formState.value = CreateRequestFormState.Idle
    }
}
