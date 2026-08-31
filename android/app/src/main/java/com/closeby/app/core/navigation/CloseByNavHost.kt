package com.closeby.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.closeby.app.feature.advertisement.CreateAdvertisementRoute
import com.closeby.app.feature.advertisement.MyAdvertisementsRoute
import com.closeby.app.feature.explore.ExploreScreen
import com.closeby.app.feature.home.HomeScreen
import com.closeby.app.feature.notification.NotificationsScreen
import com.closeby.app.feature.profile.ProfileScreen
import com.closeby.app.feature.provider.AddEditServiceRoute
import com.closeby.app.feature.provider.AvailabilityEditorRoute
import com.closeby.app.feature.provider.MyServicesRoute
import com.closeby.app.feature.provider.ProviderProfileRoute
import com.closeby.app.feature.provider.ProviderRequestsRoute
import com.closeby.app.feature.request.CreateServiceRequestRoute
import com.closeby.app.feature.request.RequestDetailsRoute
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
        composable(TopLevelDestination.Requests.route) {
            RequestsScreen(
                onOpenRequestDetails = { requestId ->
                    navController.navigate(AppRoutes.requestDetails(requestId))
                }
            )
        }
        composable(TopLevelDestination.Notifications.route) { NotificationsScreen() }
        composable(TopLevelDestination.Profile.route) {
            ProfileScreen(
                onProviderProfile = { providerId ->
                    navController.navigate(AppRoutes.providerProfile(providerId))
                },
                onMyAdvertisements = { ownerId ->
                    navController.navigate(AppRoutes.myAdvertisements(ownerId))
                },
                onCreateAdvertisement = { ownerId ->
                    navController.navigate(AppRoutes.createAdvertisement(ownerId))
                }
            )
        }

        composable(
            route = AppRoutes.SERVICE_DETAILS,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty()
            ServiceDetailsRoute(
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onViewProviderProfile = { providerId ->
                    navController.navigate(AppRoutes.providerProfile(providerId))
                },
                onRequestService = { id ->
                    navController.navigate(AppRoutes.createRequest(id))
                }
            )
        }

        composable(
            route = AppRoutes.CREATE_REQUEST,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty()
            CreateServiceRequestRoute(
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onRequestCreated = {
                    navController.popBackStack()
                    navController.navigate(TopLevelDestination.Requests.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = AppRoutes.REQUEST_DETAILS,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()
            RequestDetailsRoute(
                requestId = requestId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PROVIDER_REQUEST_DETAILS,
            arguments = listOf(
                navArgument("providerId") { type = NavType.StringType },
                navArgument("requestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()
            RequestDetailsRoute(
                requestId = requestId,
                providerId = providerId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PROVIDER_PROFILE,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            ProviderProfileRoute(
                providerId = providerId,
                onBack = { navController.popBackStack() },
                onMyServices = { navController.navigate(AppRoutes.myServices(providerId)) },
                onEditAvailability = { navController.navigate(AppRoutes.editAvailability(providerId)) },
                onServiceClick = { serviceId ->
                    navController.navigate(AppRoutes.serviceDetails(serviceId))
                },
                onProviderRequests = { navController.navigate(AppRoutes.providerRequests(providerId)) }
            )
        }

        composable(
            route = AppRoutes.MY_SERVICES,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            MyServicesRoute(
                providerId = providerId,
                onBack = { navController.popBackStack() },
                onAddService = { navController.navigate(AppRoutes.addService(providerId)) },
                onEditService = { serviceId ->
                    navController.navigate(AppRoutes.editService(providerId, serviceId))
                },
                onViewService = { serviceId ->
                    navController.navigate(AppRoutes.serviceDetails(serviceId))
                }
            )
        }

        composable(
            route = AppRoutes.ADD_SERVICE,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            AddEditServiceRoute(
                providerId = providerId,
                serviceId = null,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoutes.EDIT_SERVICE,
            arguments = listOf(
                navArgument("providerId") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            val serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty()
            AddEditServiceRoute(
                providerId = providerId,
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PROVIDER_REQUESTS,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            ProviderRequestsRoute(
                providerId = providerId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.CREATE_ADVERTISEMENT,
            arguments = listOf(navArgument("ownerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ownerId = backStackEntry.arguments?.getString("ownerId").orEmpty()
            CreateAdvertisementRoute(
                ownerId = ownerId,
                onBack = { navController.popBackStack() },
                onCreated = {
                    navController.popBackStack()
                    navController.navigate(AppRoutes.myAdvertisements(ownerId))
                }
            )
        }

        composable(
            route = AppRoutes.MY_ADVERTISEMENTS,
            arguments = listOf(navArgument("ownerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ownerId = backStackEntry.arguments?.getString("ownerId").orEmpty()
            MyAdvertisementsRoute(
                ownerId = ownerId,
                onBack = { navController.popBackStack() },
                onCreateAd = { navController.navigate(AppRoutes.createAdvertisement(ownerId)) }
            )
        }

        composable(
            route = AppRoutes.EDIT_AVAILABILITY,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            AvailabilityEditorRoute(
                providerId = providerId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
