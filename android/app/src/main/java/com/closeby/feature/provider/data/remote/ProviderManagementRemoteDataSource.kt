package com.closeby.feature.provider.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.app.data.model.ProviderDto
import com.closeby.app.data.model.ProviderInsertDto
import com.closeby.app.data.model.ProviderUpdateDto
import com.closeby.app.data.model.ServiceActiveUpdateDto
import com.closeby.app.data.model.ServiceDeleteDto
import com.closeby.app.data.model.ServiceDto
import com.closeby.app.data.model.ServiceInsertDto
import com.closeby.app.data.model.ServiceUpdateDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant

class ProviderManagementRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    private val serviceColumns = """
        id,
        provider_id,
        category,
        subcategory,
        title,
        description,
        image_urls,
        latitude,
        longitude,
        availability,
        price_amount,
        price_unit,
        price_is_starting,
        rating,
        review_count,
        is_active,
        contact_number,
        deleted_at,
        providers (
            name,
            phone_number,
            is_verified
        )
    """.trimIndent()

    suspend fun getProviderById(id: String): ProviderDto? =
        client.from("providers")
            .select { filter { eq("id", id) } }
            .decodeList<ProviderDto>()
            .firstOrNull()

    suspend fun getProviderByUserId(userId: String): ProviderDto? =
        client.from("providers")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ProviderDto>()
            .firstOrNull()

    suspend fun insertProvider(dto: ProviderInsertDto): ProviderDto =
        client.from("providers")
            .insert(dto) { select() }
            .decodeSingle<ProviderDto>()

    suspend fun updateProvider(id: String, dto: ProviderUpdateDto): ProviderDto =
        client.from("providers")
            .update(dto) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle<ProviderDto>()

    suspend fun getServicesByProvider(providerId: String, includeDeleted: Boolean = false): List<ServiceDto> =
        client.from("services")
            .select(columns = Columns.raw(serviceColumns)) {
                filter { eq("provider_id", providerId) }
            }
            .decodeList<ServiceDto>()
            .let { rows ->
                if (includeDeleted) rows else rows.filter { it.deletedAt == null }
            }

    suspend fun getServiceById(serviceId: String): ServiceDto? =
        client.from("services")
            .select(columns = Columns.raw(serviceColumns)) {
                filter { eq("id", serviceId) }
            }
            .decodeList<ServiceDto>()
            .firstOrNull()

    suspend fun insertService(dto: ServiceInsertDto): ServiceDto =
        client.from("services")
            .insert(dto) { select(columns = Columns.raw(serviceColumns)) }
            .decodeSingle<ServiceDto>()

    suspend fun updateService(serviceId: String, dto: ServiceUpdateDto): ServiceDto =
        client.from("services")
            .update(dto) {
                filter { eq("id", serviceId) }
                select(columns = Columns.raw(serviceColumns))
            }
            .decodeSingle<ServiceDto>()

    suspend fun setServiceActive(serviceId: String, isActive: Boolean) {
        client.from("services")
            .update(
                ServiceActiveUpdateDto(
                    isActive = isActive,
                    updatedAt = Instant.now().toString()
                )
            ) {
                filter { eq("id", serviceId) }
            }
    }

    suspend fun softDeleteService(serviceId: String) {
        client.from("services")
            .update(
                ServiceDeleteDto(
                    isActive = false,
                    deletedAt = Instant.now().toString()
                )
            ) {
                filter { eq("id", serviceId) }
            }
    }
}
