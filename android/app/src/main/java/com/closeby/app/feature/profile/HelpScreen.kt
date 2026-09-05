package com.closeby.app.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Help") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HelpSection(
                title = "How Close by works",
                body = "Close by helps you find nearby vehicles, labour, and equipment. Browse as a guest — no login required."
            )
            HelpSection(
                title = "Finding services",
                body = "Allow location access to see nearest services first. Use Explore to search, filter by distance, and open service details."
            )
            HelpSection(
                title = "Contacting providers",
                body = "Call and SMS buttons open your phone's native dialer and messaging apps. Payment is arranged directly with the provider — not in the app."
            )
            HelpSection(
                title = "Requesting a service",
                body = "From a service page, submit a request with date, time, duration, and budget. Budget is for negotiation only — not an online payment."
            )
            HelpSection(
                title = "Becoming a provider",
                body = "Sign in with email OTP, then choose Become a Provider from Profile. Your customer account stays separate until you complete onboarding."
            )
            HelpSection(
                title = "Safety",
                body = "Meet in safe public places when possible. Report suspicious listings or behaviour from Profile → Report a Problem."
            )
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
}
