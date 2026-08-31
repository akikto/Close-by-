package com.closeby.feature.servicelisting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.core.error.AppErrorMapper
import com.closeby.app.core.error.retryWithBackoff
import com.closeby.app.core.network.NetworkMonitor
import com.closeby.app.core.network.NetworkStatus
import com.closeby.app.data.repository.OfflineAwareServiceRepository
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ServiceListingViewModel(
    private val serviceRepository: ServiceRepository,
    private val locationProvider: LocationProvider,
    private val blockedProviderIdsProvider: suspend () -> Set<String> = { emptySet() },
    private val networkMonitor: NetworkMonitor? = null,
    private val searchServicesUseCase: SearchServicesUseCase = SearchServicesUseCase(),
    private val filterServicesUseCase: FilterServicesUseCase = FilterServicesUseCase(),
    private val sortServicesUseCase: SortServicesUseCase = SortServicesUseCase(locationProvider),
    private val pageSize: Int = PAGE_SIZE
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceListUiState>(ServiceListUiState.Loading)
    val uiState: StateFlow<ServiceListUiState> = _uiState.asStateFlow()

    private val _locationStatus = MutableStateFlow(com.closeby.feature.servicelisting.domain.model.LocationStatus.UNAVAILABLE)
    val locationStatus: StateFlow<com.closeby.feature.servicelisting.domain.model.LocationStatus> =
        _locationStatus.asStateFlow()

    private var allListings: List<ServiceListing> = emptyList()
    private var processedListings: List<ServiceListing> = emptyList()
    private var displayedCount: Int = pageSize
    private var currentQuery: String = ""
    private var currentFilter: ServiceFilter = ServiceFilter()
    private var currentSort: SortOption = SortOption.DEFAULT
    private var isOffline = false
    private var isShowingCachedData = false
    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

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
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { debounced ->
                    currentQuery = debounced
                    displayedCount = pageSize
                    applyPipeline()
                }
        }
        loadServices()
    }

    fun retryLocation() {
        locationProvider.retryLocation()
    }

    fun loadServices() {
        _uiState.value = ServiceListUiState.Loading
        displayedCount = pageSize
        viewModelScope.launch {
            isOffline = networkMonitor?.status?.value == NetworkStatus.OFFLINE
            retryWithBackoff(times = 3) {
                serviceRepository.fetchServices()
            }
                .onSuccess { listings ->
                    isShowingCachedData =
                        (serviceRepository as? OfflineAwareServiceRepository)?.isShowingCachedData == true
                    val blockedIds = blockedProviderIdsProvider()
                    val visible = listings.filter { it.providerId !in blockedIds }
                    val enriched = locationProvider.attachDistances(visible)
                    allListings = enriched
                    applyPipeline()
                }
                .onFailure { throwable ->
                    _uiState.value = ServiceListUiState.Error(
                        message = AppErrorMapper.toUserMessage(throwable),
                        isRetryable = true
                    )
                }
        }
    }

    fun retry() = loadServices()

    fun onQueryChanged(query: String) {
        queryFlow.value = query
    }

    fun loadMore() {
        val current = _uiState.value as? ServiceListUiState.Success ?: return
        if (!current.hasMore || current.isLoadingMore) return
        _uiState.value = current.copy(isLoadingMore = true)
        displayedCount += pageSize
        publishSuccess()
    }

    fun onCategorySelected(category: ServiceCategory?) {
        currentFilter = currentFilter.copy(category = category, subcategory = null)
        displayedCount = pageSize
        applyPipelineSync()
    }

    fun onSubcategorySelected(subcategory: ServiceSubcategory?) {
        currentFilter = currentFilter.copy(subcategory = subcategory)
        displayedCount = pageSize
        applyPipelineSync()
    }

    fun onFilterChanged(filter: ServiceFilter) {
        currentFilter = filter
        displayedCount = pageSize
        applyPipelineSync()
    }

    fun onSortChanged(sortOption: SortOption) {
        currentSort = sortOption
        displayedCount = pageSize
        applyPipelineSync()
    }

    fun clearFilters() {
        currentFilter = ServiceFilter()
        displayedCount = pageSize
        applyPipelineSync()
    }

    private fun applyPipelineSync() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { applyPipeline() }
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
        val sort = resolveSortOption()
        processedListings = sortServicesUseCase(filtered, sort)

        if (processedListings.isEmpty()) {
            val reason = when {
                currentQuery.isNotBlank() -> EmptyReason.NO_SEARCH_RESULTS
                currentFilter.effectiveRadiusKm != null -> EmptyReason.NO_SERVICES_IN_RADIUS
                currentFilter.category != null -> EmptyReason.NO_SERVICES_IN_CATEGORY
                else -> EmptyReason.NO_SERVICES_AVAILABLE
            }
            _uiState.value = ServiceListUiState.Empty(reason, currentQuery, currentFilter)
        } else {
            publishSuccess()
        }
    }

    private fun resolveSortOption(): SortOption {
        if (currentSort == SortOption.NEAREST_FIRST &&
            _locationStatus.value == com.closeby.feature.servicelisting.domain.model.LocationStatus.UNAVAILABLE
        ) {
            return SortOption.NEWEST
        }
        return currentSort
    }

    private fun publishSuccess() {
        val page = processedListings.take(displayedCount)
        _uiState.value = ServiceListUiState.Success(
            listings = page,
            query = currentQuery,
            filter = currentFilter,
            sortOption = currentSort,
            totalCount = processedListings.size,
            hasMore = displayedCount < processedListings.size,
            isLoadingMore = false,
            isOffline = isOffline,
            isShowingCachedData = isShowingCachedData
        )
    }

    companion object {
        const val PAGE_SIZE = 20
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
