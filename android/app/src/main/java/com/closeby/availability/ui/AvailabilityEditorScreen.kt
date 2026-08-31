package com.closeby.availability.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.availability.domain.model.ProviderAvailability
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AvailabilityEditorScreen(
    entries: List<ProviderAvailability>,
    isSaving: Boolean,
    errorMessage: String?,
    onSave: (List<ProviderAvailability>) -> Unit,
    modifier: Modifier = Modifier
) {
    var localEntries by remember(entries) { mutableStateOf(entries) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Weekly Availability", style = MaterialTheme.typography.titleLarge)
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(localEntries, key = { it.dayOfWeek }) { entry ->
                AvailabilityDayEditor(
                    entry = entry,
                    onChange = { updated ->
                        localEntries = localEntries.map {
                            if (it.dayOfWeek == updated.dayOfWeek) updated else it
                        }
                    }
                )
            }
        }
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = { onSave(localEntries) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Availability")
            }
        }
    }
}

@Composable
private fun AvailabilityDayEditor(
    entry: ProviderAvailability,
    onChange: (ProviderAvailability) -> Unit
) {
    val dayLabel = entry.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(dayLabel, modifier = Modifier.weight(1f))
            Switch(
                checked = entry.isAvailable,
                onCheckedChange = { available ->
                    onChange(
                        entry.copy(
                            isAvailable = available,
                            startTime = if (available) entry.startTime ?: LocalTime.of(8, 0) else null,
                            endTime = if (available) entry.endTime ?: LocalTime.of(18, 0) else null
                        )
                    )
                }
            )
        }
        if (entry.isAvailable) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = entry.startTime?.toString().orEmpty(),
                    onValueChange = { raw ->
                        runCatching { LocalTime.parse(raw) }.onSuccess { time ->
                            onChange(entry.copy(startTime = time))
                        }
                    },
                    label = { Text("Start") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = entry.endTime?.toString().orEmpty(),
                    onValueChange = { raw ->
                        runCatching { LocalTime.parse(raw) }.onSuccess { time ->
                            onChange(entry.copy(endTime = time))
                        }
                    },
                    label = { Text("End") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun defaultWeek(providerId: String): List<ProviderAvailability> =
    DayOfWeek.entries.map { day ->
        ProviderAvailability(
            providerId = providerId,
            dayOfWeek = day,
            isAvailable = day != DayOfWeek.SUNDAY,
            startTime = if (day == DayOfWeek.SUNDAY) null else LocalTime.of(8, 0),
            endTime = if (day == DayOfWeek.SUNDAY) null else LocalTime.of(18, 0)
        )
    }
