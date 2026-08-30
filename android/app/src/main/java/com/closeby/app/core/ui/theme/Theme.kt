package com.closeby.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TextOnBrand,
    primaryContainer = TealPrimaryLight,
    secondary = BluePrimary,
    onSecondary = TextOnBrand,
    secondaryContainer = BluePrimaryLight,
    background = SurfaceSoft,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = TealPrimaryLight,
    onPrimary = TextPrimary,
    primaryContainer = TealPrimaryDark,
    secondary = BluePrimaryLight,
    onSecondary = TextPrimary,
    secondaryContainer = BluePrimaryDark,
    background = Color0F,
    onBackground = SurfaceWhite,
    surface = Color15,
    onSurface = SurfaceWhite,
    surfaceVariant = Color15,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

// Small private helpers kept local to the dark palette so Color.kt stays
// focused on the brand tokens designers actually asked for.
private val Color0F = androidx.compose.ui.graphics.Color(0xFF0F1615)
private val Color15 = androidx.compose.ui.graphics.Color(0xFF15201F)

@Composable
fun CloseByTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CloseByTypography,
        shapes = CloseByShapes,
        content = content
    )
}
