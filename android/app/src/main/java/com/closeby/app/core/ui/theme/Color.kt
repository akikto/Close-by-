package com.closeby.app.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Close by design language: teal + blue, soft gradients, white surfaces.
 * These are the base tokens. Feature modules should reference these via
 * MaterialTheme.colorScheme rather than importing this file directly.
 */

// Brand
val TealPrimary = Color(0xFF0E9394)
val TealPrimaryDark = Color(0xFF076A6B)
val TealPrimaryLight = Color(0xFF5FC6C7)

val BluePrimary = Color(0xFF2F6FED)
val BluePrimaryDark = Color(0xFF1E4FB8)
val BluePrimaryLight = Color(0xFF7DA3F5)

// Surfaces
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceSoft = Color(0xFFF4F8F8)
val SurfaceCard = Color(0xFFFFFFFF)

// Text
val TextPrimary = Color(0xFF10201F)
val TextSecondary = Color(0xFF5B6B6A)
val TextOnBrand = Color(0xFFFFFFFF)

// Status
val SuccessGreen = Color(0xFF2E9E5B)
val WarningAmber = Color(0xFFE0A100)
val ErrorRed = Color(0xFFD64545)

// Gradient stops used by GradientSurface component
val GradientStart = TealPrimary
val GradientEnd = BluePrimary
