package com.closeby.app.feature.saved

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun SavedServiceMigrationDialog(
    state: MigrationPromptState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSuccessDismiss: () -> Unit
) {
    when (state) {
        is MigrationPromptState.Prompt -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Sync saved services?") },
            text = {
                Text(
                    "You have ${state.localCount} service(s) saved while browsing anonymously. " +
                        "Sync them to your account?"
                )
            },
            confirmButton = { TextButton(onClick = onConfirm) { Text("Sync now") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } }
        )
        MigrationPromptState.Migrating -> AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text("Syncing saved services") },
            text = { CircularProgressIndicator() },
            confirmButton = {}
        )
        is MigrationPromptState.Success -> AlertDialog(
            onDismissRequest = onSuccessDismiss,
            title = { Text("Saved services synced") },
            text = { Text("${state.migratedCount} service(s) are now on your account.") },
            confirmButton = { TextButton(onClick = onSuccessDismiss) { Text("OK") } }
        )
        is MigrationPromptState.Error -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Sync failed") },
            text = { Text(state.message) },
            confirmButton = { TextButton(onClick = onRetry) { Text("Retry") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
        )
        MigrationPromptState.Hidden -> Unit
    }
}
