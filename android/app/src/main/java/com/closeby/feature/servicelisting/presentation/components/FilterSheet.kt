package com.closeby.feature.servicelisting.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.closeby.feature.servicelisting.domain.model.AvailabilityFilter
import com.closeby.feature.servicelisting.domain.model.RadiusOption
import com.closeby.feature.servicelisting.domain.model.RatingFilter
import com.closeby.feature.servicelisting.domain.model.ServiceFilter

/**
 * Filter bottom sheet supporting Distance, Availability, Rating, and Price
 * (price is informational only — never a payment filter).
 */
@Composable
fun FilterSheet(
    filter: ServiceFilter,
    onFilterChange: (ServiceFilter) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Filters", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)

            SectionLabel("Distance")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.foundation.lazy.items(RadiusOption.entries.toList()) { radius ->
                    FilterChip(
                        selected = filter.radiusKm == radius,
                        onClick = {
                            val newRadius = if (filter.radiusKm == radius) null else radius
                            onFilterChange(filter.copy(radiusKm = newRadius))
                        },
                        label = { Text(radius.label) }
                    )
                }
            }

            SectionLabel("Availability")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AvailabilityFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter.availability == option,
                        onClick = { onFilterChange(filter.copy(availability = option)) },
                        label = { Text(option.label) }
                    )
                }
            }

            SectionLabel("Rating")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RatingFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter.minRating == option.minRating,
                        onClick = {
                            val newRating = if (filter.minRating == option.minRating) null else option.minRating
                            onFilterChange(filter.copy(minRating = newRating))
                        },
                        label = { Text(option.label) }
                    )
                }
            }

            SectionLabel("Price information")
            Text(
                text = "Shown as reference only. Payment is arranged directly between you and the provider.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onClear) { Text("Clear all") }
                Button(onClick = onApply) { Text("Apply filters") }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}
