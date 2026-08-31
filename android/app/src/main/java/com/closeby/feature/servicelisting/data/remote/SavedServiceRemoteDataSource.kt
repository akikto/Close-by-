package com.closeby.feature.servicelisting.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.feature.servicelisting.data.model.SavedServiceDto
import com.closeby.feature.servicelisting.data.model.SavedServiceInsertDto
import io.github.jan.supabase.postgrest.from

class SavedServiceRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun getByUser(userId: String): List<SavedServiceDto> =
        client.from("saved_services")
            .select { filter { eq("user_id", userId) } }
            .decodeList<SavedServiceDto>()

    suspend fun insert(dto: SavedServiceInsertDto): SavedServiceDto =
        client.from("saved_services")
            .insert(dto) { select() }
            .decodeSingle<SavedServiceDto>()

    suspend fun delete(userId: String, serviceId: String) {
        client.from("saved_services")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("service_id", serviceId)
                }
            }
    }
}
