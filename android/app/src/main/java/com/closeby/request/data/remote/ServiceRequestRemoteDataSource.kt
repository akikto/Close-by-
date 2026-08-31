package com.closeby.request.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.request.data.model.ServiceRequestDto
import com.closeby.request.data.model.ServiceRequestInsertDto
import com.closeby.request.data.model.ServiceRequestStatusUpdateDto
import io.github.jan.supabase.postgrest.from
import java.time.Instant

class ServiceRequestRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun insert(dto: ServiceRequestInsertDto): ServiceRequestDto =
        client.from("service_requests")
            .insert(dto) { select() }
            .decodeSingle<ServiceRequestDto>()

    suspend fun getByProvider(providerId: String): List<ServiceRequestDto> =
        client.from("service_requests")
            .select { filter { eq("provider_id", providerId) } }
            .decodeList<ServiceRequestDto>()

    suspend fun getByCustomer(customerId: String?): List<ServiceRequestDto> =
        client.from("service_requests")
            .select {
                filter {
                    if (customerId == null) {
                        isNull("customer_id")
                    } else {
                        eq("customer_id", customerId)
                    }
                }
            }
            .decodeList<ServiceRequestDto>()

    suspend fun updateStatus(id: String, status: String): ServiceRequestDto =
        client.from("service_requests")
            .update(
                ServiceRequestStatusUpdateDto(
                    status = status,
                    updatedAt = Instant.now().toString()
                )
            ) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle<ServiceRequestDto>()

    suspend fun getById(id: String): ServiceRequestDto? =
        client.from("service_requests")
            .select { filter { eq("id", id) } }
            .decodeList<ServiceRequestDto>()
            .firstOrNull()
}
