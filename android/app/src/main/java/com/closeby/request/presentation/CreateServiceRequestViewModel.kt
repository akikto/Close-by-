package com.closeby.request.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.request.domain.model.BudgetUnit
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.request.domain.validation.ServiceRequestValidator
import com.closeby.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class CreateServiceRequestViewModel(
    private val serviceId: String,
    private val providerId: String,
    private val serviceTitle: String,
    private val customerId: String?,
    private val repository: ServiceRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ServiceRequest>>(UiState.Idle)
    val uiState: StateFlow<UiState<ServiceRequest>> = _uiState.asStateFlow()

    fun sendRequest(
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        duration: String,
        budgetAmount: Double?,
        budgetUnit: BudgetUnit?,
        note: String?
    ) {
        val validation = ServiceRequestValidator.validateNewRequest(
            serviceTitle = serviceTitle,
            date = date,
            startTime = startTime,
            endTime = endTime,
            budgetAmount = budgetAmount
        )

        validation.onFailure { error ->
            _uiState.value = UiState.Error(error.message ?: "Invalid request.")
            return
        }

        val now = System.currentTimeMillis()
        val request = ServiceRequest(
            id = UUID.randomUUID().toString(),
            serviceId = serviceId,
            providerId = providerId,
            customerId = customerId,
            serviceTitle = serviceTitle,
            requestedDate = date,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            budgetAmount = budgetAmount,
            budgetUnit = budgetUnit,
            note = note,
            status = ServiceRequestStatus.PENDING,
            createdAt = now,
            updatedAt = now
        )

        _uiState.value = UiState.Loading
        viewModelScope.launch {
            repository.createRequest(request)
                .onSuccess { created -> _uiState.value = UiState.Success(created) }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to send request.")
                }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
