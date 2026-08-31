package com.closeby.feature.servicelisting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import com.closeby.feature.servicelisting.domain.model.SortOption
import com.closeby.feature.servicelisting.domain.repository.LocationProvider
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import com.closeby.feature.servicelisting.domain.usecase.FilterServicesUseCase
import com.closeby.feature.servicelisting.domain.usecase.SearchServicesUseCase
import com.closeby.feature.servicelisting.domain.usecase.SortServicesUseCase
import com.closeby.feature.servicelisting.presentation.model.EmptyReason
import com.closeby.feature.servicelisting.presentation.model.ServiceListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Service Listing screen.
 *
 * Architecture: UI -> ViewModel -> UseCase -> Repository interface.
 * No business logic lives in Composables; this class owns all listing
 * state transitions (loading / success / empty / error).
 */
class ServiceListingViewModel(
    private val serviceRepository: ServiceRepository,
    private val locationProvider: LocationProvider,
    private val blockedProviderIdsProvider: suspend () -> Set<String> = { emptySet() },
    private val searchServicesUseCase: SearchServicesUseCase = SearchServicesUseCase(),
    private val filterServicesUseCase: FilterServicesUseCase = FilterServicesUseCase(),
    private val sortServicesUseCase: SortServicesUseCase = SortServicesUseCase(locationProvider)
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceListUiState>(ServiceListUiState.Loading)
    val uiState: StateFlow<ServiceListUiState> = _uiState.asStateFlow()

    private val _locationStatus = MutableStateFlow(com.closeby.feature.servicelisting.domain.model.LocationStatus.UNAVAILABLE)
    val locationStatus: StateFlow<com.closeby.feature.servicelisting.domain.model.LocationStatus> =
        _locationStatus.asStateFlow()

    private var allListings: List<ServiceListing> = emptyList()
    private var currentQuery: String = ""
    private var currentFilter: ServiceFilter = ServiceFilter()
    private var currentSort: SortOption = SortOption.DEFAULT

    init {
        locationProvider.start(viewModelScope)
        viewModelScope.launch {
            locationProvider.observeLocationStatus().collect { _locationStatus.value = it }
        }
        viewModelScope.launch {
            locationProvider.observeDistanceRefresh().collect {
                if (allListings.isNotEmpty()) {
                    allListings = locationProvider.attachDistances(allListings)
                    applyPipeline()
                }
            }
        }
        loadServices()
    }

    fun retryLocation() {
        locationProvider.retryLocation()
    }

    fun loadServices() {
        _uiState.value = ServiceListUiState.Loading
        viewModelScope.launch {
            serviceRepository.fetchServices()
                .onSuccess { listings ->
                    val blockedIds = blockedProviderIdsProvider()
                    val visible = listings.filter { it.providerId !in blockedIds }
                    val enriched = locationProvider.attachDistances(visible)
                    allListings = enriched
                    applyPipeline()
                }
                .onFailure { throwable ->
                    _uiState.value = ServiceListUiState.Error(
                        message = throwable.message ?: "Something went wrong. Please try again.",
                        isRetryable = true
                    )
                }
        }
    }

    fun retry() = loadServices()

    fun onQueryChanged(query: String) {
        currentQuery = query
        applyPipelineSync()
    }

    fun onCategorySelected(category: ServiceCategory?) {
        currentFilter = currentFilter.copy(category = category, subcategory = null)
        applyPipelineSync()
    }

    fun onSubcategorySelected(subcategory: ServiceSubcategory?) {
        currentFilter = currentFilter.copy(subcategory = subcategory)
        applyPipelineSync()
    }

    fun onFilterChanged(filter: ServiceFilter) {
        currentFilter = filter
        applyPipelineSync()
    }

    fun onSortChanged(sortOption: SortOption) {
        currentSort = sortOption
        applyPipelineSync()
    }

    fun clearFilters() {
        currentFilter = ServiceFilter()
        applyPipelineSync()
    }

    /** Synchronous pipeline for search/filter (no distance recompute needed). */
    private fun applyPipelineSync() {
        viewModelScope.launch { applyPipeline() }
    }

    private suspend fun applyPipeline() {
        if (allListings.isEmpty()) {
            _uiState.value = ServiceListUiState.Empty(
                reason = EmptyReason.NO_SERVICES_AVAILABLE,
                query = currentQuery,
                filter = currentFilter
            )
            return
        }

        val searched = searchServicesUseCase(allListings, currentQuery)
        val filtered = filterServicesUseCase(searched, currentFilter)
        val sorted = sortServicesUseCase(filtered, currentSort)

        if (sorted.isEmpty()) {
            val reason = when {
                currentQuery.isNotBlank() -> EmptyReason.NO_SEARCH_RESULTS
                currentFilter.radiusKm != null -> EmptyReason.NO_SERVICES_IN_RADIUS
                currentFilter.category != null -> EmptyReason.NO_SERVICES_IN_CATEGORY
                else -> EmptyReason.NO_SERVICES_AVAILABLE
            }
            _uiState.value = ServiceListUiState.Empty(reason, currentQuery, currentFilter)
        } else {
            _uiState.value = ServiceListUiState.Success(sorted, currentQuery, currentFilter, currentSort)
        }
    }
}
