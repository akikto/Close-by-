package com.closeby.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.closeby.app.feature.home.HomeScreen
import com.closeby.app.feature.explore.ExploreScreen
import com.closeby.app.feature.request.RequestsScreen
import com.closeby.app.feature.notification.NotificationsScreen
import com.closeby.app.feature.profile.ProfileScreen

/**
 * Base navigation graph. Screens are placeholders for now — full feature
 * UI (service listings, provider dashboard, etc.) is out of scope for the
 * base project and will be built by later feature-specific work.
 */
@Composable
fun CloseByNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Home.route,
        modifier = modifier
    ) {
        composable(TopLevelDestination.Home.route) { HomeScreen() }
        composable(TopLevelDestination.Explore.route) { ExploreScreen() }
        composable(TopLevelDestination.Requests.route) { RequestsScreen() }
        composable(TopLevelDestination.Notifications.route) { NotificationsScreen() }
        composable(TopLevelDestination.Profile.route) { ProfileScreen() }
    }
}
