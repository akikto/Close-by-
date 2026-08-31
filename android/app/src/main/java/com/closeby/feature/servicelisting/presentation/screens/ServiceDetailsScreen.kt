package com.closeby.feature.servicelisting.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.closeby.feature.servicelisting.presentation.components.ServiceListErrorState
import com.closeby.feature.servicelisting.presentation.components.ServiceListLoadingState
import com.closeby.feature.servicelisting.presentation.model.ServiceDetailsUiState
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceDetailsActions
import com.closeby.feature.servicelisting.presentation.viewmodel.ServiceDetailsViewModel

/**
 * Foundation for the Service Details screen.
 *
 * "Call Provider" and "SMS Provider" only invoke the [ServiceDetailsActions]
 * callbacks — the Base Project wires these to the native Phone/SMS apps.
 * No in-app calling or chat is implemented here.
 */
@Composable
fun ServiceDetailsScreen(
    viewModel: ServiceDetailsViewModel,
    actions: ServiceDetailsActions,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ServiceDetailsUiState.Loading -> ServiceListLoadingState(modifier = modifier.fillMaxSize())

        is ServiceDetailsUiState.Error -> ServiceListErrorState(
            message = state.message,
            isRetryable = state.isRetryable,
            onRetry = viewModel::retry,
            modifier = modifier.fillMaxSize()
        )

        is ServiceDetailsUiState.Success -> {
            val listing = state.listing
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = listing.imageUrls.firstOrNull(),
                    contentDescription = "Photo of ${listing.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                )

                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "${listing.category.displayName} / ${listing.subcategory.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Text("${listing.rating} (${listing.reviewCount} reviews)")
                    listing.distanceInfo?.formatted()?.let { Text("· $it") }
                }

                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.price.formatted(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "  ${listing.availability.label}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    if (listing.isVerifiedProvider) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary)
                        Text(" Verified provider — ${listing.providerName}")
                    } else {
                        Text(listing.providerName)
                    }
                }

                TextButton(onClick = { actions.onViewProviderProfile(listing.providerId) }) {
                    Text("View provider profile")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            actions.onRequestService(
                                listing.id,
                                listing.providerId,
                                listing.title,
                                listing.providerName,
                                listing.contactNumber
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Request Service")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { actions.onCallProvider(listing.contactNumber) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null)
                        Text("  Call Provider")
                    }
                    OutlinedButton(
                        onClick = { actions.onSmsProvider(listing.contactNumber) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Message, contentDescription = null)
                        Text("  SMS Provider")
                    }
                }
            }
        }
    }
}
