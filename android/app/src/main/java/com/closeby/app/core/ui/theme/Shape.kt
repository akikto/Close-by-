package com.closeby.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Rounded-card / rounded-icon foundation for Close by.
 * Compact, friendly, large-touch-target feel.
 */
val CloseByShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Extra tokens for components that need shapes outside the M3 scale
object CloseByRadii {
    val Chip = RoundedCornerShape(50)
    val IconContainer = RoundedCornerShape(14.dp)
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}
