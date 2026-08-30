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
