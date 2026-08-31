package com.closeby.advertisement.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.advertisement.data.model.AdvertisementAdminUpdateDto
import com.closeby.advertisement.data.model.AdvertisementDto
import com.closeby.advertisement.data.model.AdvertisementInsertDto
import com.closeby.advertisement.domain.model.AdStatus
import io.github.jan.supabase.postgrest.from
import java.time.Instant

class AdvertisementRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun insert(dto: AdvertisementInsertDto): AdvertisementDto =
        client.from("advertisements")
            .insert(dto) { select() }
            .decodeSingle<AdvertisementDto>()

    suspend fun getByOwner(ownerId: String): List<AdvertisementDto> =
        client.from("advertisements")
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<AdvertisementDto>()

    suspend fun getApproved(): List<AdvertisementDto> =
        client.from("advertisements")
            .select { filter { eq("status", AdStatus.APPROVED.name) } }
            .decodeList<AdvertisementDto>()

    suspend fun getPending(): List<AdvertisementDto> =
        client.from("advertisements")
            .select { filter { eq("status", AdStatus.PENDING.name) } }
            .decodeList<AdvertisementDto>()

    suspend fun getById(id: String): AdvertisementDto? =
        client.from("advertisements")
            .select { filter { eq("id", id) } }
            .decodeList<AdvertisementDto>()
            .firstOrNull()

    suspend fun updateAdmin(id: String, dto: AdvertisementAdminUpdateDto): AdvertisementDto =
        client.from("advertisements")
            .update(dto) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle<AdvertisementDto>()

    suspend fun updateStatus(
        id: String,
        status: AdStatus,
        approvedBy: String? = null,
        approvedAt: String? = null,
        rejectionReason: String? = null
    ): AdvertisementDto = updateAdmin(
        id = id,
        dto = AdvertisementAdminUpdateDto(
            status = status.name,
            approvedBy = approvedBy,
            approvedAt = approvedAt,
            rejectionReason = rejectionReason,
            updatedAt = Instant.now().toString()
        )
    )
}
