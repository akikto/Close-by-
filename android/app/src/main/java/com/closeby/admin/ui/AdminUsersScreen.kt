package com.closeby.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.admin.domain.model.AdminUserSummary
import com.closeby.util.UiState

@Composable
fun AdminUsersScreen(
    uiState: UiState<List<AdminUserSummary>>,
    onToggleSuspend: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is UiState.Idle, is UiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            if (uiState.data.isEmpty()) {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users")
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.data, key = { it.userId }) { user ->
                        UserCard(
                            user = user,
                            onToggleSuspend = { suspend -> onToggleSuspend(user.userId, suspend) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: AdminUserSummary,
    onToggleSuspend: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                user.displayName ?: user.userId.take(8),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Admin: ${user.isAdmin} • Suspended: ${user.isSuspended}",
                style = MaterialTheme.typography.bodySmall
            )
            if (!user.isAdmin) {
                if (user.isSuspended) {
                    Button(
                        onClick = { onToggleSuspend(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Unsuspend")
                    }
                } else {
                    OutlinedButton(
                        onClick = { onToggleSuspend(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Suspend")
                    }
                }
            }
        }
    }
}
