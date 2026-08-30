package com.closeby.feature.nearby.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.closeby.feature.nearby.model.SearchRadius

/**
 * Row of selectable radius chips (1/5/10/25 km) plus a "Custom" affordance.
 *
 * This composable is presentation-only: it reports the chosen [SearchRadius] via
 * [onRadiusSelected] and does not itself talk to a ViewModel.
 */
@Composable
fun RadiusSelector(
    selected: SearchRadius,
    onRadiusSelected: (SearchRadius) -> Unit,
    modifier: Modifier = Modifier,
    onCustomRequested: (() -> Unit)? = null
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SearchRadius.presets) { option ->
            FilterChip(
                selected = isSameRadius(selected, option),
                onClick = { onRadiusSelected(option) },
                label = { Text(labelFor(option)) }
            )
        }
        if (onCustomRequested != null) {
            item {
                FilterChip(
                    selected = selected is SearchRadius.Custom,
                    onClick = onCustomRequested,
                    label = {
                        Text(
                            if (selected is SearchRadius.Custom) {
                                "${selected.km.toInt()} km"
                            } else {
                                "Custom"
                            }
                        )
                    }
                )
            }
        }
    }
}

private fun isSameRadius(a: SearchRadius, b: SearchRadius): Boolean =
    a::class == b::class && a.kilometers == b.kilometers

private fun labelFor(radius: SearchRadius): String = when (radius) {
    SearchRadius.OneKm -> "1 km"
    SearchRadius.FiveKm -> "5 km"
    SearchRadius.TenKm -> "10 km"
    SearchRadius.TwentyFiveKm -> "25 km"
    is SearchRadius.Custom -> "${radius.km.toInt()} km"
}

@Preview
@Composable
private fun RadiusSelectorPreview() {
    Row {
        RadiusSelector(selected = SearchRadius.FiveKm, onRadiusSelected = {})
    }
}
