package com.closeby.app.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.closeby.app.core.di.NotificationDependenciesFactory
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.app.core.navigation.CloseByBottomBar
import com.closeby.app.core.navigation.CloseByNavHost
import com.closeby.app.core.ui.theme.CloseByTheme
import com.closeby.notification.presentation.NotificationUnreadHolder

/**
 * App root: theme + bottom navigation + nav host.
 * This is the base scaffold — no feature logic lives here.
 */
@Composable
fun CloseByApp() {
    CloseByTheme {
        val navController = rememberNavController()
        val context = LocalContext.current
        val unreadCount by NotificationUnreadHolder.unreadCount.collectAsState()

        LaunchedEffect(Unit) {
            ProviderDependenciesFactory.clientSessionStorage(context).getOrCreateSessionId()
            NotificationDependenciesFactory.ensureEventHandlerStarted(context)
            val userId = ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId
            if (!userId.isNullOrBlank()) {
                NotificationDependenciesFactory.notificationRepository()
                    .getUnreadCount(userId)
                    .onSuccess { NotificationUnreadHolder.update(it) }
            }
        }

        Scaffold(
            bottomBar = { CloseByBottomBar(navController, notificationsUnreadCount = unreadCount) }
        ) { padding ->
            CloseByNavHost(
                navController = navController,
                modifier = androidx.compose.ui.Modifier.padding(padding)
            )
        }
    }
}
