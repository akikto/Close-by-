package com.closeby.advertisement.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.advertisement.data.storage.AdImageUploader
import com.closeby.advertisement.domain.model.AdRadiusPreset
import com.closeby.advertisement.domain.model.AdvertisementInput
import com.closeby.advertisement.domain.repository.AdvertisementRepository
import com.closeby.advertisement.domain.validation.AdValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

sealed class CreateAdUiState {
    data object Idle : CreateAdUiState()
    data object Saving : CreateAdUiState()
    data class Saved(val adId: String) : CreateAdUiState()
    data class ValidationError(val message: String) : CreateAdUiState()
    data class Error(val message: String) : CreateAdUiState()
    data class Ready(
        val businessName: String,
        val title: String,
        val description: String,
        val contactNumber: String,
        val latitude: Double,
        val longitude: Double,
        val radiusPreset: AdRadiusPreset,
        val customRadiusKm: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val imagePreview: String?
    ) : CreateAdUiState()
}

class CreateAdvertisementViewModel(
    private val ownerId: String,
    private val repository: AdvertisementRepository,
    private val imageUploader: AdImageUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateAdUiState>(CreateAdUiState.Idle)
    val uiState: StateFlow<CreateAdUiState> = _uiState.asStateFlow()

    private var pendingImageUri: Uri? = null

    fun load(initialLatitude: Double? = null, initialLongitude: Double? = null) {
        val today = LocalDate.now()
        val lat = initialLatitude
        val lng = initialLongitude
        if (lat == null || lng == null) {
            _uiState.value = CreateAdUiState.Error(
                "Advertisement location is required. Enable GPS and try again."
            )
            return
        }
        _uiState.value = CreateAdUiState.Ready(
            businessName = "",
            title = "",
            description = "",
            contactNumber = "",
            latitude = lat,
            longitude = lng,
            radiusPreset = AdRadiusPreset.KM_5,
            customRadiusKm = "5",
            startDate = today,
            endDate = today.plusDays(7),
            imagePreview = null
        )
    }

    fun updateForm(transform: (CreateAdUiState.Ready) -> CreateAdUiState.Ready) {
        val current = _uiState.value
        if (current is CreateAdUiState.Ready) {
            _uiState.value = transform(current)
        }
    }

    fun queueImage(uri: Uri) {
        pendingImageUri = uri
        updateForm { ready -> ready.copy(imagePreview = uri.toString()) }
    }

    fun save(ready: CreateAdUiState.Ready) {
        viewModelScope.launch {
            val imageUrl = uploadImageIfNeeded()
            val input = ready.toInput(imageUrl)
            AdValidator.validate(input).onFailure { error ->
                _uiState.value = CreateAdUiState.ValidationError(
                    error.message ?: "Please check the form."
                )
                return@launch
            }
            _uiState.value = CreateAdUiState.Saving
            repository.createAd(ownerId, input)
                .onSuccess { ad -> _uiState.value = CreateAdUiState.Saved(ad.id) }
                .onFailure { error ->
                    _uiState.value = CreateAdUiState.Error(error.message ?: "Failed to create ad.")
                }
        }
    }

    private suspend fun uploadImageIfNeeded(): String? {
        val uri = pendingImageUri ?: return (_uiState.value as? CreateAdUiState.Ready)?.imagePreview
        return imageUploader.upload(uri, ownerId).getOrElse { throw it }
    }

    private fun CreateAdUiState.Ready.toInput(imageUrl: String?): AdvertisementInput {
        val zone = ZoneId.systemDefault()
        val radiusMeters = resolveRadiusMeters()
        return AdvertisementInput(
            businessName = businessName,
            title = title,
            description = description,
            imageUrl = imageUrl,
            contactNumber = contactNumber,
            latitude = latitude,
            longitude = longitude,
            targetRadiusMeters = radiusMeters,
            startAt = startDate.atStartOfDay(zone).toInstant().toEpochMilli(),
            endAt = endDate.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant().toEpochMilli()
        )
    }

    private fun CreateAdUiState.Ready.resolveRadiusMeters(): Int =
        if (radiusPreset == AdRadiusPreset.CUSTOM) {
            (customRadiusKm.toDoubleOrNull()?.times(1000)?.toInt()) ?: AdRadiusPreset.KM_5.meters
        } else {
            radiusPreset.meters
        }
}
