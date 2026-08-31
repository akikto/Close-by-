package com.closeby.app.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.app.data.model.ProviderDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Thin Supabase/PostgreSQL access layer for provider data. No business
 * logic here — that belongs in the domain/usecase layer. Table name and
 * query shape are placeholders pending the real schema design.
 */
class ProviderRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun getProviderById(id: String): ProviderDto? {
        return client.from("providers")
            .select(columns = Columns.ALL) {
                filter { eq("id", id) }
            }
            .decodeList<ProviderDto>()
            .firstOrNull()
    }

    suspend fun getAllProviders(): List<ProviderDto> {
        return client.from("providers")
            .select(columns = Columns.ALL)
            .decodeList<ProviderDto>()
    }
}
