package com.closeby.feature.servicelisting.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory

/**
 * Single-row category selector: Vehicles / Labour / Equipment.
 * Selecting a category reveals its subcategories via [SubcategorySelector].
 * Intentionally single-level — no nested navigation.
 */
@Composable
fun CategorySelector(
    selectedCategory: ServiceCategory?,
    onCategorySelected: (ServiceCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(ServiceCategory.entries.toList()) { category ->
            val selected = category == selectedCategory
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(if (selected) null else category) },
                label = { Text("${category.emoji} ${category.displayName}") },
                modifier = Modifier.semantics {
                    contentDescription = "${category.displayName} category" + if (selected) ", selected" else ""
                },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

@Composable
fun SubcategorySelector(
    category: ServiceCategory,
    selectedSubcategory: ServiceSubcategory?,
    onSubcategorySelected: (ServiceSubcategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(category.subcategories()) { subcategory ->
            val selected = subcategory == selectedSubcategory
            FilterChip(
                selected = selected,
                onClick = { onSubcategorySelected(if (selected) null else subcategory) },
                label = { Text(subcategory.displayName) },
                modifier = Modifier.semantics {
                    contentDescription = "${subcategory.displayName} subcategory" + if (selected) ", selected" else ""
                }
            )
        }
    }
}
