package com.closeby.app.core.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

private val topLevelRouteSet: Set<String> = topLevelDestinations.map { it.route }.toSet()

internal fun isTopLevelRoute(route: String?): Boolean = route in topLevelRouteSet

fun NavDestination?.isTopLevelRoute(): Boolean = isTopLevelRoute(this?.route)

/**
 * Navigate to a bottom-nav tab, clearing any detail/nested routes (e.g. service
 * details) that sit above the tab roots on the back stack.
 */
fun NavHostController.navigateToTopLevelDestination(route: String) {
    val currentRoute = currentBackStackEntry?.destination?.route
    val onTopLevel = currentRoute in topLevelRouteSet

    if (onTopLevel && currentRoute == route) {
        return
    }

    val startRoute = graph.findStartDestination().route ?: TopLevelDestination.Home.route

    if (!onTopLevel) {
        popBackStack(startRoute, inclusive = false, saveState = true)
        if (currentBackStackEntry?.destination?.route == route) {
            return
        }
    }

    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
