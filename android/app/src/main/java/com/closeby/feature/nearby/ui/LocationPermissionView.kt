package com.closeby.feature.nearby.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Shown when location permission has not been granted. Two flavors depending on
 * whether the OS will still show a permission dialog ([permanentlyDenied] = false)
 * or the user must go to app settings ([permanentlyDenied] = true).
 */
@Composable
fun LocationPermissionView(
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Location access needed")
        Text(
            text = if (permanentlyDenied) {
                "Close by needs your location to show nearby services. Enable it in app settings."
            } else {
                "Close by uses your location to find vehicles, labour and equipment near you."
            }
        )
        if (permanentlyDenied) {
            Button(onClick = onOpenSettings) {
                Text("Open settings")
            }
        } else {
            Button(onClick = onRequestPermission) {
                Text("Allow location access")
            }
        }
    }
}

@Preview
@Composable
private fun LocationPermissionViewPreview() {
    LocationPermissionView(
        permanentlyDenied = false,
        onRequestPermission = {},
        onOpenSettings = {}
    )
}
