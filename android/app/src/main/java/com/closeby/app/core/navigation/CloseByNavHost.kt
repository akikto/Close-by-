package com.closeby.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.closeby.app.core.di.ProviderDependenciesFactory
import com.closeby.app.feature.admin.AdminAdvertisementsRoute
import com.closeby.app.feature.admin.AdminGateRoute
import com.closeby.app.feature.admin.AdminLoadingRoute
import com.closeby.app.feature.admin.AdminProvidersRoute
import com.closeby.app.feature.admin.AdminReportsRoute
import com.closeby.app.feature.admin.AdminServicesRoute
import com.closeby.app.feature.admin.AdminUsersRoute
import com.closeby.app.feature.admin.AdminVerificationRoute
import com.closeby.app.feature.advertisement.CreateAdvertisementRoute
import com.closeby.app.feature.advertisement.MyAdvertisementsRoute
import com.closeby.app.feature.blocked.BlockedProvidersRoute
import com.closeby.app.feature.explore.ExploreScreen
import com.closeby.app.feature.home.HomeScreen
import com.closeby.app.feature.notification.NotificationsRoute
import com.closeby.app.feature.profile.ProfileScreen
import com.closeby.app.feature.saved.RecentlyViewedRoute
import com.closeby.app.feature.saved.SavedServicesRoute
import com.closeby.app.feature.provider.AddEditServiceRoute
import com.closeby.app.feature.provider.AvailabilityEditorRoute
import com.closeby.app.feature.provider.MyServicesRoute
import com.closeby.app.feature.provider.ProviderProfileRoute
import com.closeby.app.feature.provider.ProviderRequestsRoute
import com.closeby.app.feature.request.CreateServiceRequestRoute
import com.closeby.app.feature.request.RequestDetailsRoute
import com.closeby.app.feature.request.RequestsScreen
import com.closeby.app.feature.servicedetails.ServiceDetailsRoute
import com.closeby.app.feature.trust.ReportRoute
import com.closeby.app.feature.trust.SubmitReviewRoute
import com.closeby.app.feature.trust.VerificationRoute
import com.closeby.trust.domain.model.ReportTargetType
import com.closeby.trust.domain.model.ReviewerRole

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
        composable(TopLevelDestination.Notifications.route) {
            NotificationsRoute(
                onOpenRequestDetails = { requestId ->
                    navController.navigate(AppRoutes.requestDetails(requestId))
                },
                onOpenProviderRequestDetails = { providerId, requestId ->
                    navController.navigate(AppRoutes.providerRequestDetails(providerId, requestId))
                },
                onOpenVerification = {
                    navController.navigate(TopLevelDestination.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onOpenAdvertisement = { _ ->
                    val ownerId = runBlocking {
                        ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId
                    }
                    if (!ownerId.isNullOrBlank()) {
                        navController.navigate(AppRoutes.myAdvertisements(ownerId))
                    } else {
                        navController.navigate(TopLevelDestination.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                },
                onOpenAdmin = {
                    navController.navigate(AppRoutes.ADMIN)
                },
                onOpenProfile = {
                    navController.navigate(TopLevelDestination.Profile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
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
                },
                onAdminDashboard = {
                    navController.navigate(AppRoutes.ADMIN)
                },
                onMyRequests = {
                    navController.navigate(TopLevelDestination.Requests.route) {
                        launchSingleTop = true
                    }
                },
                onSavedServices = {
                    navController.navigate(AppRoutes.SAVED_SERVICES)
                },
                onRecentlyViewed = {
                    navController.navigate(AppRoutes.RECENTLY_VIEWED)
                },
                onBlockedProviders = {
                    navController.navigate(AppRoutes.BLOCKED_PROVIDERS)
                },
                onReportProblem = {
                    navController.navigate(AppRoutes.report(ReportTargetType.SERVICE.name, "support"))
                }
            )
        }

        composable(AppRoutes.SAVED_SERVICES) {
            SavedServicesRoute(
                onBack = { navController.popBackStack() },
                onServiceClick = { serviceId ->
                    navController.navigate(AppRoutes.serviceDetails(serviceId))
                }
            )
        }

        composable(AppRoutes.RECENTLY_VIEWED) {
            RecentlyViewedRoute(
                onBack = { navController.popBackStack() },
                onServiceClick = { serviceId ->
                    navController.navigate(AppRoutes.serviceDetails(serviceId))
                }
            )
        }

        composable(AppRoutes.BLOCKED_PROVIDERS) {
            BlockedProvidersRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.ADMIN) {
            var userId by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                userId = withContext(Dispatchers.IO) {
                    ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId
                }
            }
            val resolvedUserId = userId
            if (resolvedUserId == null) {
                AdminLoadingRoute()
            } else {
                AdminGateRoute(
                    userId = resolvedUserId,
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
        }

        composable(AppRoutes.ADMIN_VERIFICATIONS) {
            AdminVerificationRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.ADMIN_REPORTS) {
            AdminReportsRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.ADMIN_ADS) {
            AdminAdvertisementsRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.ADMIN_PROVIDERS) {
            AdminProvidersRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.ADMIN_SERVICES) {
            AdminServicesRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.ADMIN_USERS) {
            AdminUsersRoute(onBack = { navController.popBackStack() })
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
                },
                onReportService = { id ->
                    navController.navigate(AppRoutes.report(ReportTargetType.SERVICE.name, id))
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
                onBack = { navController.popBackStack() },
                onLeaveReview = { id ->
                    navController.navigate(AppRoutes.submitReview(id, ReviewerRole.CUSTOMER.name))
                }
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
                onBack = { navController.popBackStack() },
                onLeaveReview = { id ->
                    navController.navigate(AppRoutes.submitReview(id, ReviewerRole.PROVIDER.name))
                }
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
                onProviderRequests = { navController.navigate(AppRoutes.providerRequests(providerId)) },
                onVerification = { navController.navigate(AppRoutes.verification(providerId)) },
                onReportProvider = {
                    navController.navigate(AppRoutes.report(ReportTargetType.PROVIDER.name, providerId))
                }
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

        composable(
            route = AppRoutes.VERIFICATION,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId").orEmpty()
            VerificationRoute(
                providerId = providerId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.SUBMIT_REVIEW,
            arguments = listOf(
                navArgument("requestId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()
            val roleRaw = backStackEntry.arguments?.getString("role").orEmpty()
            val role = runCatching { ReviewerRole.valueOf(roleRaw.uppercase()) }
                .getOrDefault(ReviewerRole.CUSTOMER)
            SubmitReviewRoute(
                requestId = requestId,
                role = role,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.REPORT,
            arguments = listOf(
                navArgument("targetType") { type = NavType.StringType },
                navArgument("targetId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val targetTypeRaw = backStackEntry.arguments?.getString("targetType").orEmpty()
            val targetId = backStackEntry.arguments?.getString("targetId").orEmpty()
            val targetType = runCatching { ReportTargetType.valueOf(targetTypeRaw.uppercase()) }
                .getOrDefault(ReportTargetType.SERVICE)
            ReportRoute(
                targetType = targetType,
                targetId = targetId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
