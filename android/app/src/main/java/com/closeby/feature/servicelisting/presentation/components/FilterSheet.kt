package com.closeby.feature.servicelisting.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.closeby.feature.servicelisting.domain.model.AvailabilityFilter
import com.closeby.feature.servicelisting.domain.model.RadiusOption
import com.closeby.feature.servicelisting.domain.model.RatingFilter
import com.closeby.feature.servicelisting.domain.model.ServiceFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Filter bottom sheet supporting Distance, Availability, Rating, and Price
 * (price is informational only — never a payment filter).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    filter: ServiceFilter,
    onFilterChange: (ServiceFilter) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var customRadiusText by remember(filter.customRadiusKm) {
        mutableStateOf(filter.customRadiusKm?.toString().orEmpty())
    }
    var maxPriceText by remember(filter.maxPrice) {
        mutableStateOf(filter.maxPrice?.toString().orEmpty())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = dateState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onFilterChange(
                            filter.copy(
                                availability = AvailabilityFilter.AVAILABLE_ON_DATE,
                                availabilityDate = date
                            )
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Filters", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)

            SectionLabel("Distance")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RadiusOption.entries.toList()) { radius ->
                    FilterChip(
                        selected = filter.radiusKm == radius && filter.customRadiusKm == null,
                        onClick = {
                            val newRadius = if (filter.radiusKm == radius) null else radius
                            onFilterChange(filter.copy(radiusKm = newRadius, customRadiusKm = null))
                            customRadiusText = ""
                        },
                        label = { Text(radius.label) }
                    )
                }
            }
            OutlinedTextField(
                value = customRadiusText,
                onValueChange = { value ->
                    customRadiusText = value
                    val km = value.toDoubleOrNull()
                    onFilterChange(
                        filter.copy(
                            customRadiusKm = km,
                            radiusKm = if (km != null) null else filter.radiusKm
                        )
                    )
                },
                label = { Text("Custom radius (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true
            )

            SectionLabel("Availability")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AvailabilityFilter.entries.toList()) { option ->
                    FilterChip(
                        selected = filter.availability == option,
                        onClick = {
                            if (option == AvailabilityFilter.AVAILABLE_ON_DATE) {
                                showDatePicker = true
                            } else {
                                onFilterChange(
                                    filter.copy(availability = option, availabilityDate = null)
                                )
                            }
                        },
                        label = { Text(option.label) }
                    )
                }
            }
            filter.availabilityDate?.let { date ->
                Text(
                    text = "Selected date: $date",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
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

            SectionLabel("Maximum price (reference)")
            OutlinedTextField(
                value = maxPriceText,
                onValueChange = { value ->
                    maxPriceText = value
                    onFilterChange(filter.copy(maxPrice = value.toDoubleOrNull()))
                },
                label = { Text("Max price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
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
