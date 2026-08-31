package com.closeby.app.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.closeby.app.core.di.NotificationDependenciesFactory
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.model.NotificationReferenceType
import com.closeby.notification.domain.model.NotificationType
import com.closeby.notification.presentation.NotificationsUiState
import com.closeby.notification.presentation.NotificationsViewModel
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.util.Date

@Composable
fun NotificationsRoute(
    onOpenRequestDetails: (requestId: String) -> Unit = {},
    onOpenProviderRequestDetails: (providerId: String, requestId: String) -> Unit = { _, _ -> },
    onOpenVerification: () -> Unit = {},
    onOpenAdvertisement: (adId: String) -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        NotificationDependenciesFactory.ensureEventHandlerStarted(context)
    }

    val userId = remember {
        runBlocking { ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId }
    }

    val viewModel: NotificationsViewModel = viewModel(
        factory = remember(userId) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    NotificationsViewModel(
                        userId = userId,
                        repository = NotificationDependenciesFactory.notificationRepository()
                    ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    NotificationsScreen(
        uiState = uiState,
        onMarkAllRead = viewModel::markAllRead,
        onNotificationClick = { notification ->
            viewModel.markRead(notification)
            navigateFromNotification(
                notification = notification,
                onOpenRequestDetails = onOpenRequestDetails,
                onOpenProviderRequestDetails = onOpenProviderRequestDetails,
                onOpenVerification = onOpenVerification,
                onOpenAdvertisement = onOpenAdvertisement,
                onOpenAdmin = onOpenAdmin,
                onOpenProfile = onOpenProfile
            )
        },
        onRetry = viewModel::load
    )
}

private fun navigateFromNotification(
    notification: AppNotification,
    onOpenRequestDetails: (String) -> Unit,
    onOpenProviderRequestDetails: (providerId: String, requestId: String) -> Unit,
    onOpenVerification: () -> Unit,
    onOpenAdvertisement: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenProfile: () -> Unit
) {
    when (notification.referenceType) {
        NotificationReferenceType.REQUEST -> {
            val requestId = notification.referenceId ?: return
            when (notification.type) {
                NotificationType.NEW_PROVIDER_REQUEST,
                NotificationType.REQUEST_CANCELLED ->
                    onOpenProviderRequestDetails("", requestId)
                NotificationType.REQUEST_ACCEPTED,
                NotificationType.REQUEST_REJECTED,
                NotificationType.REQUEST_COMPLETED -> onOpenRequestDetails(requestId)
                else -> onOpenRequestDetails(requestId)
            }
        }
        NotificationReferenceType.VERIFICATION -> onOpenVerification()
        NotificationReferenceType.AD -> notification.referenceId?.let(onOpenAdvertisement)
        NotificationReferenceType.ADMIN -> onOpenAdmin()
        NotificationReferenceType.ACCOUNT -> onOpenProfile()
        NotificationReferenceType.REVIEW,
        NotificationReferenceType.REPORT -> notification.referenceId?.let(onOpenRequestDetails)
        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onMarkAllRead: () -> Unit = {},
    onNotificationClick: (AppNotification) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val hasUnread = (uiState as? NotificationsUiState.Loaded)?.notifications?.any { !it.isRead } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    if (hasUnread) {
                        TextButton(onClick = onMarkAllRead) {
                            Text("Mark all read")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                NotificationsUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                NotificationsUiState.Empty -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No notifications yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Sign in to receive updates on your requests.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is NotificationsUiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp)
                    )
                    TextButton(onClick = onRetry) { Text("Retry") }
                }
                is NotificationsUiState.Loaded -> LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            onClick = { onNotificationClick(notification) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val background = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    }
    val titleWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = titleWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatTimestamp(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(epochMillis))
