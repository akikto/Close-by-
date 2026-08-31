package com.closeby.advertisement.ui

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
import com.closeby.advertisement.domain.model.AdRadiusPreset
import com.closeby.advertisement.presentation.CreateAdUiState
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdvertisementScreen(
    state: CreateAdUiState,
    onSave: (CreateAdUiState.Ready) -> Unit,
    onUpdate: ((CreateAdUiState.Ready) -> CreateAdUiState.Ready) -> Unit,
    onPickImage: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is CreateAdUiState.Idle -> {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is CreateAdUiState.Saving -> {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text("Submitting…", modifier = Modifier.padding(top = 12.dp))
            }
        }
        is CreateAdUiState.ValidationError -> {
            FormContent(
                ready = defaultReady(),
                error = state.message,
                onSave = onSave,
                onUpdate = onUpdate,
                onPickImage = onPickImage,
                modifier = modifier
            )
        }
        is CreateAdUiState.Error -> {
            Column(
                modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is CreateAdUiState.Saved -> Unit
        is CreateAdUiState.Ready -> FormContent(
            ready = state,
            error = null,
            onSave = onSave,
            onUpdate = onUpdate,
            onPickImage = onPickImage,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    ready: CreateAdUiState.Ready,
    error: String?,
    onSave: (CreateAdUiState.Ready) -> Unit,
    onUpdate: ((CreateAdUiState.Ready) -> CreateAdUiState.Ready) -> Unit,
    onPickImage: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let(onPickImage) }

    var radiusExpanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = ready.businessName,
            onValueChange = { value -> onUpdate { it.copy(businessName = value) } },
            label = { Text("Business name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ready.title,
            onValueChange = { value -> onUpdate { it.copy(title = value) } },
            label = { Text("Offer title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ready.description,
            onValueChange = { value -> onUpdate { it.copy(description = value) } },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ready.contactNumber,
            onValueChange = { value -> onUpdate { it.copy(contactNumber = value) } },
            label = { Text("Contact number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                Text(if (ready.imagePreview == null) "Add image" else "Change image")
            }
        }
        ready.imagePreview?.let { preview ->
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = preview,
                contentDescription = "Ad preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        ExposedDropdownMenuBox(
            expanded = radiusExpanded,
            onExpandedChange = { radiusExpanded = it }
        ) {
            OutlinedTextField(
                value = ready.radiusPreset.name.replace('_', ' '),
                onValueChange = {},
                readOnly = true,
                label = { Text("Target radius") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = radiusExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = radiusExpanded,
                onDismissRequest = { radiusExpanded = false }
            ) {
                AdRadiusPreset.entries.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.name.replace('_', ' ')) },
                        onClick = {
                            onUpdate { it.copy(radiusPreset = preset) }
                            radiusExpanded = false
                        }
                    )
                }
            }
        }

        if (ready.radiusPreset == AdRadiusPreset.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ready.customRadiusKm,
                onValueChange = { value -> onUpdate { it.copy(customRadiusKm = value) } },
                label = { Text("Custom radius (km)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ready.startDate.format(dateFormatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("Start date") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ready.endDate.format(dateFormatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("End date") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSave(ready) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Submit for review")
        }
    }
}

private fun defaultReady() = CreateAdUiState.Ready(
    businessName = "",
    title = "",
    description = "",
    contactNumber = "",
    latitude = 12.9716,
    longitude = 77.5946,
    radiusPreset = AdRadiusPreset.KM_5,
    customRadiusKm = "5",
    startDate = java.time.LocalDate.now(),
    endDate = java.time.LocalDate.now().plusDays(7),
    imagePreview = null
)
