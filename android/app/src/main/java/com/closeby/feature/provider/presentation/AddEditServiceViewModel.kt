package com.closeby.feature.provider.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.data.storage.ServiceImageUploader
import com.closeby.feature.provider.domain.model.ManagedService
import com.closeby.feature.provider.domain.model.ServiceFormInput
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.feature.provider.domain.validation.ServiceFormValidator
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ServiceFormUiState {
    data object Idle : ServiceFormUiState()
    data object Loading : ServiceFormUiState()
    data object Saving : ServiceFormUiState()
    data class Saved(val serviceId: String) : ServiceFormUiState()
    data class ValidationError(val message: String) : ServiceFormUiState()
    data class Error(val message: String) : ServiceFormUiState()
    data class Ready(
        val serviceId: String?,
        val category: ServiceCategory,
        val subcategory: ServiceSubcategory,
        val title: String,
        val description: String,
        val latitude: Double,
        val longitude: Double,
        val availability: AvailabilityStatus,
        val contactNumber: String,
        val imageUrls: List<String>,
        val priceText: String,
        val priceUnit: PriceUnit?,
        val priceIsStarting: Boolean
    ) : ServiceFormUiState()
}

class AddEditServiceViewModel(
    private val providerId: String,
    private val serviceId: String?,
    private val repository: ProviderManagementRepository,
    private val imageUploader: ServiceImageUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceFormUiState>(ServiceFormUiState.Idle)
    val uiState: StateFlow<ServiceFormUiState> = _uiState.asStateFlow()

    private var pendingUploads = mutableListOf<Uri>()

    fun load() {
        if (serviceId == null) {
            _uiState.value = ServiceFormUiState.Ready(
                serviceId = null,
                category = ServiceCategory.EQUIPMENT,
                subcategory = ServiceSubcategory.WATER_PUMP,
                title = "",
                description = "",
                latitude = 12.9716,
                longitude = 77.5946,
                availability = AvailabilityStatus.AVAILABLE_NOW,
                contactNumber = "",
                imageUrls = emptyList(),
                priceText = "",
                priceUnit = null,
                priceIsStarting = false
            )
            return
        }
        _uiState.value = ServiceFormUiState.Loading
        viewModelScope.launch {
            repository.getManagedService(serviceId, providerId)
                .onSuccess { service -> _uiState.value = service.toReadyState() }
                .onFailure { error ->
                    _uiState.value = ServiceFormUiState.Error(error.message ?: "Failed to load service.")
                }
        }
    }

    fun updateForm(transform: (ServiceFormUiState.Ready) -> ServiceFormUiState.Ready) {
        val current = _uiState.value
        if (current is ServiceFormUiState.Ready) {
            _uiState.value = transform(current)
        }
    }

    fun queueImage(uri: Uri) {
        pendingUploads.add(uri)
        updateForm { ready ->
            ready.copy(imageUrls = ready.imageUrls + uri.toString())
        }
    }

    fun removeImage(index: Int) {
        updateForm { ready ->
            val urls = ready.imageUrls.toMutableList()
            if (index in urls.indices) urls.removeAt(index)
            if (index in pendingUploads.indices) pendingUploads.removeAt(index)
            ready.copy(imageUrls = urls)
        }
    }

    fun save(ready: ServiceFormUiState.Ready) {
        val input = ready.toInput()
        ServiceFormValidator.validate(input).onFailure { error ->
            _uiState.value = ServiceFormUiState.ValidationError(
                error.message ?: "Please check the form."
            )
            return
        }
        _uiState.value = ServiceFormUiState.Saving
        viewModelScope.launch {
            val uploadedUrls = uploadPendingImages(ready.imageUrls)
            val finalInput = input.copy(imageUrls = uploadedUrls)
            val result = if (serviceId == null) {
                repository.createService(providerId, finalInput)
            } else {
                repository.updateService(serviceId, providerId, finalInput)
            }
            result
                .onSuccess { saved ->
                    pendingUploads.clear()
                    _uiState.value = ServiceFormUiState.Saved(saved.id)
                }
                .onFailure { error ->
                    _uiState.value = ServiceFormUiState.Error(error.message ?: "Could not save service.")
                }
        }
    }

    private suspend fun uploadPendingImages(currentUrls: List<String>): List<String> {
        val result = mutableListOf<String>()
        var pendingIndex = 0
        for (url in currentUrls) {
            if (url.startsWith("content://") || url.startsWith("file://")) {
                val uri = Uri.parse(url)
                val uploaded = imageUploader.upload(uri, providerId).getOrElse {
                    throw it
                }
                result.add(uploaded)
                pendingIndex++
            } else {
                result.add(url)
            }
        }
        return result
    }

    private fun ManagedService.toReadyState() = ServiceFormUiState.Ready(
        serviceId = id,
        category = category,
        subcategory = subcategory,
        title = title,
        description = description,
        latitude = latitude,
        longitude = longitude,
        availability = availability,
        contactNumber = contactNumber,
        imageUrls = imageUrls,
        priceText = price?.amount?.toString().orEmpty(),
        priceUnit = price?.unit?.takeIf { it != PriceUnit.NONE },
        priceIsStarting = price?.isStartingPrice == true
    )

    private fun ServiceFormUiState.Ready.toInput(): ServiceFormInput {
        val priceAmount = priceText.toDoubleOrNull()
        return ServiceFormInput(
            category = category,
            subcategory = subcategory,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            availability = availability,
            contactNumber = contactNumber,
            imageUrls = imageUrls,
            priceAmount = priceAmount,
            priceUnit = priceUnit,
            priceIsStarting = priceIsStarting
        )
    }
}
