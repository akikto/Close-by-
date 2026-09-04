package com.closeby.app.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.closeby.app.core.navigation.TopLevelDestination
import com.closeby.feature.servicelisting.domain.model.ServiceCategory

data class ScreenAccent(
    val primary: Color,
    val secondary: Color,
    val onAccent: Color = TextOnBrand
) {
    val gradientStart: Color get() = primary
    val gradientEnd: Color get() = secondary
}

object ScreenAccents {
    val Home = ScreenAccent(TealPrimary, BluePrimary)
    val Explore = ScreenAccent(Color(0xFF2563EB), Color(0xFF60A5FA))
    val Requests = ScreenAccent(Color(0xFFD97706), Color(0xFFFBBF24))
    val Notifications = ScreenAccent(Color(0xFF9333EA), Color(0xFFC084FC))
    val Profile = ScreenAccent(Color(0xFFE11D48), Color(0xFFFB7185))

    fun forRoute(route: String): ScreenAccent = when (route) {
        TopLevelDestination.Home.route -> Home
        TopLevelDestination.Explore.route -> Explore
        TopLevelDestination.Requests.route -> Requests
        TopLevelDestination.Notifications.route -> Notifications
        TopLevelDestination.Profile.route -> Profile
        else -> Home
    }

    fun forDestination(destination: TopLevelDestination): ScreenAccent = forRoute(destination.route)

    fun forCategory(category: ServiceCategory): Color = when (category) {
        ServiceCategory.VEHICLES -> Color(0xFF2563EB)
        ServiceCategory.LABOUR -> Color(0xFFEA580C)
        ServiceCategory.EQUIPMENT -> Color(0xFF7C3AED)
    }

    fun categorySurface(category: ServiceCategory): Color = forCategory(category).copy(alpha = 0.12f)
}
