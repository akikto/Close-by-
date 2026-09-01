package com.closeby.app.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.closeby.app.R

/**
 * Official Close by brand mark — use this composable anywhere the app logo
 * should appear instead of ad-hoc text or placeholder icons.
 */
@Composable
fun CloseByLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    contentDescription: String = stringResource(R.string.app_name)
) {
    Image(
        painter = painterResource(R.drawable.ic_closeby_logo),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun CloseByBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 72.dp,
    subtitle: String? = null,
    subtitleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        CloseByLogo(size = logoSize)
        subtitle?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
