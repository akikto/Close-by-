package com.closeby.feature.servicelisting.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.closeby.app.core.ui.theme.ScreenAccents
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory

@Composable
fun CategorySelector(
    selectedCategory: ServiceCategory?,
    onCategorySelected: (ServiceCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(ServiceCategory.entries.toList()) { category ->
            val selected = category == selectedCategory
            val accent = ScreenAccents.forCategory(category)
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(if (selected) null else category) },
                label = {
                    Text(
                        "${category.emoji} ${category.displayName}",
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                modifier = Modifier.semantics {
                    contentDescription = "${category.displayName} category" + if (selected) ", selected" else ""
                },
                shape = RoundedCornerShape(12.dp),
                colors = categoryChipColors(accent)
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
    val accent = ScreenAccents.forCategory(category)
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 2.dp),
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
                },
                shape = RoundedCornerShape(12.dp),
                colors = categoryChipColors(accent)
            )
        }
    }
}

@Composable
private fun categoryChipColors(accent: Color) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = accent.copy(alpha = 0.18f),
    selectedLabelColor = accent,
    containerColor = MaterialTheme.colorScheme.surface,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    iconColor = accent
)
