package com.closeby.feature.servicelisting.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.feature.servicelisting.domain.model.ServiceListing
import com.closeby.feature.servicelisting.presentation.components.CategorySelector
import com.closeby.feature.servicelisting.presentation.components.FilterSheet
import com.closeby.feature.servicelisting.presentation.components.ServiceCard
import com.closeby.feature.servicelisting.presentation.components.ServiceListEmptyState
import com.closeby.feature.servicelisting.presentation.components.ServiceListErrorState
import com.closeby.feature.servicelisting.presentation.components.ServiceListLoadingState
import com.closeby.feature.servicelisting.presentation.components.ServiceSearchBar
import com.closeby.feature.servicelisting.presentation.components.SortSelector
import com.closeby.feature.servicelisting.presentation.components.SubcategorySelector
import com.closeby.feature.servicelisting.presentation.model.ServiceListUiState
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceListingViewModel

/**
 * Top level Service Listing screen: search + category/subcategory + filter
 * + sort, rendering all required states (loading/success/empty/error).
 *
 * This composable contains NO business logic — all state transitions are
 * owned by [ServiceListingViewModel].
 */
@Composable
fun ServiceListingScreen(
    viewModel: ServiceListingViewModel,
    onServiceClick: (ServiceListing) -> Unit,
    modifier: Modifier = Modifier,
    showFullFilters: Boolean = true,
    maxListings: Int? = null,
    showSearchBar: Boolean = true,
    savedServiceIds: Set<String> = emptySet(),
    onToggleSave: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        if (showSearchBar) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                ServiceSearchBar(
                    query = (uiState as? ServiceListUiState.Success)?.query
                        ?: (uiState as? ServiceListUiState.Empty)?.query.orEmpty(),
                    onQueryChange = viewModel::onQueryChanged,
                    modifier = Modifier.weight(1f)
                )
                if (showFullFilters) {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Open filters")
                    }
                }
            }
        }

        val currentFilter = when (uiState) {
            is ServiceListUiState.Success -> (uiState as ServiceListUiState.Success).filter
            is ServiceListUiState.Empty -> (uiState as ServiceListUiState.Empty).filter
            else -> com.closeby.feature.servicelisting.domain.model.ServiceFilter()
        }

        if (showFullFilters) {
            CategorySelector(
                selectedCategory = currentFilter.category,
                onCategorySelected = viewModel::onCategorySelected,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (showFullFilters) {
            currentFilter.category?.let { category ->
                SubcategorySelector(
                    category = category,
                    selectedSubcategory = currentFilter.subcategory,
                    onSubcategorySelected = viewModel::onSubcategorySelected,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val sortOption = (uiState as? ServiceListUiState.Success)?.sortOption
                    ?: com.closeby.feature.servicelisting.domain.model.SortOption.DEFAULT
                SortSelector(selected = sortOption, onSortSelected = viewModel::onSortChanged)
            }
        }

        when (val state = uiState) {
            is ServiceListUiState.Loading -> ServiceListLoadingState(modifier = Modifier.fillMaxSize())

            is ServiceListUiState.Success -> {
                val listings = maxListings?.let { limit ->
                    state.listings.take(limit)
                } ?: state.listings
                Text(
                    text = "${state.totalCount} services found",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listings, key = { it.id }) { listing ->
                        ServiceCard(
                            listing = listing,
                            onClick = onServiceClick,
                            isSaved = listing.id in savedServiceIds,
                            onToggleSave = onToggleSave?.let { toggle -> { toggle(listing.id) } }
                        )
                    }
                    if (maxListings == null && state.hasMore) {
                        item {
                            Button(
                                onClick = viewModel::loadMore,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isLoadingMore
                            ) {
                                if (state.isLoadingMore) {
                                    CircularProgressIndicator()
                                } else {
                                    Text("Load more")
                                }
                            }
                        }
                    }
                }
            }

            is ServiceListUiState.Empty -> ServiceListEmptyState(
                reason = state.reason,
                onClearFilters = viewModel::clearFilters,
                modifier = Modifier.fillMaxSize()
            )

            is ServiceListUiState.Error -> ServiceListErrorState(
                message = state.message,
                isRetryable = state.isRetryable,
                onRetry = viewModel::retry,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showFilterSheet && showFullFilters) {
        FilterSheet(
            filter = currentFilterOrDefault(uiState),
            onFilterChange = viewModel::onFilterChanged,
            onApply = { showFilterSheet = false },
            onClear = { viewModel.clearFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }
}

private fun currentFilterOrDefault(
    uiState: ServiceListUiState
): com.closeby.feature.servicelisting.domain.model.ServiceFilter = when (uiState) {
    is ServiceListUiState.Success -> uiState.filter
    is ServiceListUiState.Empty -> uiState.filter
    else -> com.closeby.feature.servicelisting.domain.model.ServiceFilter()
}
