package com.closeby.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level destinations shown in the bottom navigation bar.
 * Each maps to a route in [com.closeby.app.core.navigation.CloseByNavHost].
 */
sealed class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : TopLevelDestination("home", "Home", Icons.Filled.Home)
    data object Explore : TopLevelDestination("explore", "Explore", Icons.Filled.Explore)
    data object Requests : TopLevelDestination("requests", "Requests", Icons.Filled.Assignment)
    data object Notifications : TopLevelDestination("notifications", "Notifications", Icons.Filled.Notifications)
    data object Profile : TopLevelDestination("profile", "Profile", Icons.Filled.Person)
}

val topLevelDestinations = listOf(
    TopLevelDestination.Home,
    TopLevelDestination.Explore,
    TopLevelDestination.Requests,
    TopLevelDestination.Notifications,
    TopLevelDestination.Profile
)

object AppRoutes {
    const val SERVICE_DETAILS = "service/{serviceId}"
    const val PROVIDER_PROFILE = "provider/{providerId}"
    const val MY_SERVICES = "provider/my-services/{providerId}"
    const val ADD_SERVICE = "provider/add-service/{providerId}"
    const val EDIT_SERVICE = "provider/edit-service/{providerId}/{serviceId}"
    const val PROVIDER_REQUESTS = "provider/requests/{providerId}"
    const val EDIT_AVAILABILITY = "provider/availability/{providerId}"

    const val VERIFICATION = "verification/{providerId}"
    const val SUBMIT_REVIEW = "review/{requestId}/{role}"
    const val REPORT = "report/{targetType}/{targetId}"

    const val CREATE_REQUEST = "service/{serviceId}/request"
    const val REQUEST_DETAILS = "request/{requestId}"
    const val PROVIDER_REQUEST_DETAILS = "provider/{providerId}/request/{requestId}"

    fun serviceDetails(serviceId: String): String = "service/$serviceId"
    fun createRequest(serviceId: String): String = "service/$serviceId/request"
    fun requestDetails(requestId: String): String = "request/$requestId"
    fun providerRequestDetails(providerId: String, requestId: String): String =
        "provider/$providerId/request/$requestId"
    fun providerProfile(providerId: String): String = "provider/$providerId"
    fun myServices(providerId: String): String = "provider/my-services/$providerId"
    fun addService(providerId: String): String = "provider/add-service/$providerId"
    fun editService(providerId: String, serviceId: String): String =
        "provider/edit-service/$providerId/$serviceId"
    fun providerRequests(providerId: String): String = "provider/requests/$providerId"
    fun editAvailability(providerId: String): String = "provider/availability/$providerId"
    fun verification(providerId: String): String = "verification/$providerId"
    fun submitReview(requestId: String, role: String): String = "review/$requestId/$role"
    fun report(targetType: String, targetId: String): String = "report/$targetType/$targetId"
}
