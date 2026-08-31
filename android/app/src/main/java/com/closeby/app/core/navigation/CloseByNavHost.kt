package com.closeby.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.closeby.app.feature.explore.ExploreScreen
import com.closeby.app.feature.home.HomeScreen
import com.closeby.app.feature.notification.NotificationsScreen
import com.closeby.app.feature.profile.ProfileScreen
import com.closeby.app.feature.request.RequestsScreen
import com.closeby.app.feature.servicedetails.ServiceDetailsRoute

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
        composable(TopLevelDestination.Home.route) {
            HomeScreen(
                onServiceClick = { listing ->
                    navController.navigate(AppRoutes.serviceDetails(listing.id))
                },
                onExploreSearch = {
                    navController.navigate(TopLevelDestination.Explore.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(TopLevelDestination.Explore.route) {
            ExploreScreen(
                onServiceClick = { listing ->
                    navController.navigate(AppRoutes.serviceDetails(listing.id))
                }
            )
        }
        composable(TopLevelDestination.Requests.route) { RequestsScreen() }
        composable(TopLevelDestination.Notifications.route) { NotificationsScreen() }
        composable(TopLevelDestination.Profile.route) { ProfileScreen() }

        composable(
            route = AppRoutes.SERVICE_DETAILS,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty()
            ServiceDetailsRoute(
                serviceId = serviceId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
