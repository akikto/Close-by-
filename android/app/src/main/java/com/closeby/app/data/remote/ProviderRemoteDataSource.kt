package com.closeby.app.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.app.data.model.ProviderDto
import io.github.jan_tennert.supabase.postgrest.postgrest
import io.github.jan_tennert.supabase.postgrest.query.Columns

/**
 * Thin Supabase/PostgreSQL access layer for provider data. No business
 * logic here — that belongs in the domain/usecase layer. Table name and
 * query shape are placeholders pending the real schema design.
 */
class ProviderRemoteDataSource(
    private val client: io.github.jan_tennert.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun getProviderById(id: String): ProviderDto? {
        return client.postgrest.from("providers")
            .select(columns = Columns.ALL) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull()
    }

    suspend fun getAllProviders(): List<ProviderDto> {
        return client.postgrest.from("providers")
            .select(columns = Columns.ALL)
            .decodeList()
    }
}
