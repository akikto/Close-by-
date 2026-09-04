package com.closeby.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.closeby.app.core.ui.theme.GradientEnd
import com.closeby.app.core.ui.theme.GradientStart
import com.closeby.app.core.ui.theme.ScreenAccent

/**
 * Base reusable pieces for the "teal/blue, soft gradients, rounded cards,
 * large touch targets" design language. Feature screens should build on
 * these rather than restyling from scratch.
 */

@Composable
fun CloseByCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun GradientSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 24,
    gradientStart: Color = GradientStart,
    gradientEnd: Color = GradientEnd,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(listOf(gradientStart, gradientEnd)),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
    ) {
        content()
    }
}

@Composable
fun ScreenPageHeader(
    title: String,
    accent: ScreenAccent,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    GradientSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 0,
        gradientStart = accent.gradientStart,
        gradientEnd = accent.gradientEnd
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent.onAccent
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent.onAccent.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp), // large touch target
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
