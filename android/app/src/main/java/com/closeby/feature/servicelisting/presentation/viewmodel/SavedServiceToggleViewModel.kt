package com.closeby.feature.servicelisting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedServiceToggleViewModel(
    private val repository: SavedServiceRepository
) : ViewModel() {

    val savedIds: StateFlow<Set<String>> = repository.observeSavedServiceIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val inFlight = mutableSetOf<String>()

    fun toggle(serviceId: String) {
        if (serviceId in inFlight) return
        inFlight.add(serviceId)
        viewModelScope.launch {
            runCatching {
                if (repository.isSaved(serviceId)) {
                    repository.unsave(serviceId)
                } else {
                    repository.save(serviceId)
                }
            }
            inFlight.remove(serviceId)
        }
    }

    fun isSaved(serviceId: String, ids: Set<String>): Boolean = serviceId in ids
}
