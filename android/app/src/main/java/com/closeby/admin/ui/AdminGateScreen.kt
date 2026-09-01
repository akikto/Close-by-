package com.closeby.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.closeby.app.core.ui.components.CloseByBrandHeader
import com.closeby.app.core.ui.components.CloseByLogo
import com.closeby.admin.domain.model.AdminDashboardStats
import com.closeby.util.UiState

data class AdminNavItem(
    val title: String,
    val value: String,
    val route: String
)

@Composable
fun AdminGateScreen(
    gateState: com.closeby.admin.presentation.AdminGateUiState,
    dashboardState: UiState<AdminDashboardStats>,
    onRetry: () -> Unit,
    onNavigate: (String) -> Unit,
    onRefreshDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (gateState) {
        com.closeby.admin.presentation.AdminGateUiState.Checking -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CloseByBrandHeader(logoSize = 72.dp, subtitle = "Loading admin tools...")
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
        is com.closeby.admin.presentation.AdminGateUiState.Denied -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CloseByBrandHeader(logoSize = 72.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = gateState.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
            }
        }
        com.closeby.admin.presentation.AdminGateUiState.Authorized -> {
            AdminDashboardScreen(
                uiState = dashboardState,
                onRefresh = onRefreshDashboard,
                onNavigate = onNavigate,
                modifier = modifier
            )
        }
    }
}

@Composable
fun AdminDashboardScreen(
    uiState: UiState<AdminDashboardStats>,
    onRefresh: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Admin Dashboard", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Platform overview and moderation tools",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
        CloseByLogo(size = 48.dp)
        Spacer(modifier = Modifier.height(8.dp))

        when (uiState) {
            is UiState.Idle, is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRefresh, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Retry")
                    }
                }
            }
            is UiState.Success -> {
                val stats = uiState.data
                val items = listOf(
                    AdminNavItem("Users", stats.totalUsers.toString(), "admin/users"),
                    AdminNavItem("Providers", stats.totalProviders.toString(), "admin/providers"),
                    AdminNavItem("Active Services", stats.activeServices.toString(), "admin/services"),
                    AdminNavItem("Pending Verifications", stats.pendingVerifications.toString(), "admin/verifications"),
                    AdminNavItem("Pending Ads", stats.pendingAdvertisements.toString(), "admin/ads"),
                    AdminNavItem("Open Reports", stats.openReports.toString(), "admin/reports")
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        AdminStatCard(item = item, onClick = { onNavigate(item.route) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(item: AdminNavItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
