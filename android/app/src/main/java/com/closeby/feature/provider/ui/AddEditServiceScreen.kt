package com.closeby.feature.provider.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.closeby.feature.provider.presentation.ServiceFormUiState
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditServiceScreen(
    state: ServiceFormUiState,
    onSave: (ServiceFormUiState.Ready) -> Unit,
    onUpdate: ((ServiceFormUiState.Ready) -> ServiceFormUiState.Ready) -> Unit,
    onPickImage: (android.net.Uri) -> Unit,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is ServiceFormUiState.Idle, is ServiceFormUiState.Loading -> {
            Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
        is ServiceFormUiState.Saving -> {
            Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
                Text("Saving…", modifier = Modifier.padding(top = 12.dp))
            }
        }
        is ServiceFormUiState.ValidationError -> {
            FormContent(
                ready = defaultReady(),
                error = state.message,
                onSave = onSave,
                onUpdate = onUpdate,
                onPickImage = onPickImage,
                onRemoveImage = onRemoveImage,
                modifier = modifier
            )
        }
        is ServiceFormUiState.Error -> {
            Column(modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is ServiceFormUiState.Saved -> Unit
        is ServiceFormUiState.Ready -> FormContent(
            ready = state,
            error = null,
            onSave = onSave,
            onUpdate = onUpdate,
            onPickImage = onPickImage,
            onRemoveImage = onRemoveImage,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    ready: ServiceFormUiState.Ready,
    error: String?,
    onSave: (ServiceFormUiState.Ready) -> Unit,
    onUpdate: ((ServiceFormUiState.Ready) -> ServiceFormUiState.Ready) -> Unit,
    onPickImage: (android.net.Uri) -> Unit,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onPickImage)
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var availabilityExpanded by remember { mutableStateOf(false) }
    var priceUnitExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (ready.serviceId == null) "Add Service" else "Edit Service",
            style = MaterialTheme.typography.titleLarge
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        CategoryDropdown("Category", ready.category.displayName, categoryExpanded, { categoryExpanded = it }) {
            ServiceCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName) },
                    onClick = {
                        categoryExpanded = false
                        onUpdate { r ->
                            r.copy(category = category, subcategory = category.subcategories().first())
                        }
                    }
                )
            }
        }
        CategoryDropdown("Subcategory", ready.subcategory.displayName, subcategoryExpanded, { subcategoryExpanded = it }) {
            ready.category.subcategories().forEach { sub ->
                DropdownMenuItem(
                    text = { Text(sub.displayName) },
                    onClick = {
                        subcategoryExpanded = false
                        onUpdate { it.copy(subcategory = sub) }
                    }
                )
            }
        }
        OutlinedTextField(
            value = ready.title,
            onValueChange = { v -> onUpdate { it.copy(title = v) } },
            label = { Text("Service title *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ready.description,
            onValueChange = { v -> onUpdate { it.copy(description = v) } },
            label = { Text("Description *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        OutlinedTextField(
            value = ready.contactNumber,
            onValueChange = { v -> onUpdate { it.copy(contactNumber = v) } },
            label = { Text("Contact number *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "${ready.latitude}, ${ready.longitude}",
            onValueChange = { },
            readOnly = true,
            label = { Text("Location (lat/lng internal)") },
            modifier = Modifier.fillMaxWidth()
        )
        CategoryDropdown("Availability", ready.availability.label, availabilityExpanded, { availabilityExpanded = it }) {
            AvailabilityStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = {
                        availabilityExpanded = false
                        onUpdate { it.copy(availability = status) }
                    }
                )
            }
        }
        OutlinedTextField(
            value = ready.priceText,
            onValueChange = { v -> onUpdate { it.copy(priceText = v.filter { c -> c.isDigit() || c == '.' }) } },
            label = { Text("Price (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        CategoryDropdown(
            "Price unit",
            ready.priceUnit?.label ?: "Select unit",
            priceUnitExpanded,
            { priceUnitExpanded = it }
        ) {
            listOf(PriceUnit.HOUR, PriceUnit.DAY, PriceUnit.TRIP, PriceUnit.JOB).forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.label) },
                    onClick = {
                        priceUnitExpanded = false
                        onUpdate { it.copy(priceUnit = unit) }
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Starting Price") },
                onClick = {
                    priceUnitExpanded = false
                    onUpdate { it.copy(priceIsStarting = true, priceUnit = PriceUnit.NONE) }
                }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Starting price")
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = ready.priceIsStarting, onCheckedChange = { v -> onUpdate { it.copy(priceIsStarting = v) } })
        }

        Text("Photos (optional)", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(ready.imageUrls) { index, url ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.height(80.dp).fillMaxWidth(0.35f),
                        contentScale = ContentScale.Crop
                    )
                    OutlinedButton(onClick = { onRemoveImage(index) }) { Text("Remove") }
                }
            }
        }
        OutlinedButton(onClick = { picker.launch("image/*") }, shape = RoundedCornerShape(12.dp)) {
            Text("Add photo")
        }

        Button(onClick = { onSave(ready) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text("Save Service")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            content()
        }
    }
}

private fun defaultReady() = ServiceFormUiState.Ready(
    serviceId = null,
    category = ServiceCategory.EQUIPMENT,
    subcategory = ServiceSubcategory.WATER_PUMP,
    title = "",
    description = "",
    latitude = 12.9716,
    longitude = 77.5946,
    availability = AvailabilityStatus.AVAILABLE_NOW,
    contactNumber = "",
    imageUrls = emptyList(),
    priceText = "",
    priceUnit = null,
    priceIsStarting = false
)
