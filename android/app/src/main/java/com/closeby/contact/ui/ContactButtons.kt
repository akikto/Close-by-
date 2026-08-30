package com.closeby.contact.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.contact.data.AndroidContactLauncher
import com.closeby.contact.domain.ContactLauncher
import kotlinx.coroutines.launch

/**
 * [ 📞 Call ] button. Never places the call itself — opens the native
 * dialer via [ContactLauncher.call] and surfaces a friendly Snackbar
 * message if the phone app is unavailable.
 */
@Composable
fun CallProviderButton(
    phoneNumber: String,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            val result = contactLauncher.call(phoneNumber)
            result.onFailure { error ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        error.message ?: "Unable to open the phone app."
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
    ) {
        Icon(Icons.Filled.Call, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Call")
    }
}

/**
 * [ 💬 SMS ] button. Opens the native SMS app pre-filled with a draft
 * message — never sends automatically.
 */
@Composable
fun SmsProviderButton(
    phoneNumber: String,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    message: String = AndroidContactLauncher.DEFAULT_SMS_MESSAGE
) {
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            val result = contactLauncher.sms(phoneNumber, message)
            result.onFailure { error ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        error.message ?: "Unable to open the SMS app."
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Icon(Icons.Filled.Sms, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("SMS")
    }
}

/** Convenience row combining both buttons, used on provider/service detail screens. */
@Composable
fun ContactActionsRow(
    phoneNumber: String,
    contactLauncher: ContactLauncher,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        CallProviderButton(
            phoneNumber = phoneNumber,
            contactLauncher = contactLauncher,
            snackbarHostState = snackbarHostState
        )
        Spacer(modifier = Modifier.width(12.dp))
        SmsProviderButton(
            phoneNumber = phoneNumber,
            contactLauncher = contactLauncher,
            snackbarHostState = snackbarHostState
        )
    }
}
