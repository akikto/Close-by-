package com.closeby.app.feature.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.SavedDependenciesFactory
import com.closeby.app.core.di.ServiceRepositoryFactory
import com.closeby.feature.servicelisting.presentation.components.ServiceCard
import com.closeby.feature.servicelisting.presentation.viewmodel.RecentlyViewedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyViewedRoute(
    onBack: () -> Unit,
    onServiceClick: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: RecentlyViewedViewModel = viewModel(
        factory = remember {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RecentlyViewedViewModel(
                        historyRepository = SavedDependenciesFactory.recentlyViewedRepository(context),
                        serviceRepository = ServiceRepositoryFactory.create(context)
                    ) as T
            }
        }
    )
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Viewed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            RecentlyViewedViewModel.UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            RecentlyViewedViewModel.UiState.Empty -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { Text("No recently viewed services.") }
            is RecentlyViewedViewModel.UiState.Error -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(s.message)
                TextButton(onClick = viewModel::load) { Text("Retry") }
            }
            is RecentlyViewedViewModel.UiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(s.listings, key = { it.id }) { listing ->
                    ServiceCard(listing = listing, onClick = { onServiceClick(listing.id) })
                }
            }
        }
    }
}
