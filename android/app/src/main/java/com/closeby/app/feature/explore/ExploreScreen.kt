package com.closeby.app.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.closeby.app.feature.nearby.NearbyServicesHost
import com.closeby.feature.servicelisting.domain.model.ServiceListing

/**
 * Explore screen — full nearby search with filters, powered by real device
 * location and Supabase service data (Phase 2).
 */
@Composable
fun ExploreScreen(
    onServiceClick: (ServiceListing) -> Unit,
    modifier: Modifier = Modifier
) {
    NearbyServicesHost(
        onServiceClick = onServiceClick,
        showFullFilters = true,
        modifier = modifier
    )
}
