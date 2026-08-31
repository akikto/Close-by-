package com.closeby.availability.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.availability.data.model.ProviderAvailabilityDto
import com.closeby.availability.data.model.ProviderAvailabilityUpsertDto
import io.github.jan.supabase.postgrest.from

class AvailabilityRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun getByProvider(providerId: String): List<ProviderAvailabilityDto> =
        client.from("provider_availability")
            .select { filter { eq("provider_id", providerId) } }
            .decodeList<ProviderAvailabilityDto>()

    suspend fun replaceAll(providerId: String, entries: List<ProviderAvailabilityUpsertDto>) {
        client.from("provider_availability")
            .delete { filter { eq("provider_id", providerId) } }
        if (entries.isNotEmpty()) {
            client.from("provider_availability").insert(entries)
        }
    }
}
