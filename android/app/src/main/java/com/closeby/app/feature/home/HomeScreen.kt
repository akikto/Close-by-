package com.closeby.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.closeby.advertisement.ui.LocalOffersSection
import com.closeby.app.core.ui.components.CloseByLogo
import com.closeby.app.core.ui.components.GradientSurface
import com.closeby.app.core.ui.theme.ScreenAccents
import com.closeby.app.feature.nearby.NearbyServicesHost
import com.closeby.feature.servicelisting.domain.model.ServiceListing

/**
 * Home screen with nearby services preview, category shortcuts, and search entry.
 */
@Composable
fun HomeScreen(
    onServiceClick: (ServiceListing) -> Unit,
    onExploreSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeAccent = ScreenAccents.Home
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientSurface(
                modifier = Modifier.fillMaxWidth(),
                gradientStart = homeAccent.gradientStart,
                gradientEnd = homeAccent.gradientEnd
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CloseByLogo(size = 48.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Your Location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = homeAccent.onAccent.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        placeholder = {
                            Text(
                                "Search services...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onExploreSearch() },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = homeAccent.onAccent,
                            disabledBorderColor = homeAccent.onAccent.copy(alpha = 0.45f),
                            disabledPlaceholderColor = homeAccent.onAccent.copy(alpha = 0.75f),
                            disabledContainerColor = homeAccent.onAccent.copy(alpha = 0.12f)
                        )
                    )
                }
            }

            LocalOffersSection(
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Nearby Services",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            NearbyServicesHost(
                onServiceClick = onServiceClick,
                showFullFilters = false,
                showSearchBar = false,
                maxListings = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}
