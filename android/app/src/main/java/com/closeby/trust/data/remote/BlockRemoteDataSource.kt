package com.closeby.trust.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.trust.data.model.UserBlockDto
import com.closeby.trust.data.model.UserBlockInsertDto
import io.github.jan.supabase.postgrest.from

class BlockRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun insert(dto: UserBlockInsertDto): UserBlockDto =
        client.from("user_blocks")
            .insert(dto) { select() }
            .decodeSingle<UserBlockDto>()

    suspend fun getByBlocker(blockerId: String): List<UserBlockDto> =
        client.from("user_blocks")
            .select { filter { eq("blocker_id", blockerId) } }
            .decodeList<UserBlockDto>()

    suspend fun deleteProviderBlock(blockerId: String, providerId: String) {
        client.from("user_blocks")
            .delete {
                filter {
                    eq("blocker_id", blockerId)
                    eq("blocked_provider_id", providerId)
                }
            }
    }
}
