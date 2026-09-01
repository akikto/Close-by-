package com.closeby.app.core.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.closeby.app.core.ui.theme.ScreenAccents

@Composable
fun CloseByBottomBar(
    navController: NavHostController,
    notificationsUnreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = navBackStackEntry?.destination

    NavigationBar(modifier = modifier) {
        topLevelDestinations.forEach { destination ->
            val accent = ScreenAccents.forDestination(destination)
            val selected = currentDestination.isTopLevelRoute() &&
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTopLevelDestination(destination.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent.primary,
                    selectedTextColor = accent.primary,
                    indicatorColor = accent.primary.copy(alpha = 0.14f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    if (destination == TopLevelDestination.Notifications && notificationsUnreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = if (notificationsUnreadCount > 99) "99+"
                                        else notificationsUnreadCount.toString()
                                    )
                                }
                            }
                        ) {
                            Icon(destination.icon, contentDescription = destination.label)
                        }
                    } else {
                        Icon(destination.icon, contentDescription = destination.label)
                    }
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}
