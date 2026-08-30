package com.closeby.feature.servicelisting.presentation.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.closeby.feature.servicelisting.domain.model.SortOption

/**
 * Sort dropdown. Default is Nearest First per spec. Nearest First relies on
 * distance data supplied by the Location module.
 */
@Composable
fun SortSelector(
    selected: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    TextButton(
        onClick = { expanded = true },
        modifier = modifier.semantics { contentDescription = "Sort by, currently ${selected.label}" }
    ) {
        Text(selected.label)
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        SortOption.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                    onSortSelected(option)
                    expanded = false
                }
            )
        }
    }
}
