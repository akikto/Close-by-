package com.closeby.app.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.app.data.model.ServiceDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Supabase/PostgreSQL access layer for service listings.
 * Joins provider contact + verification fields in a single query.
 */
class ServiceRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun getAllActiveServices(): List<ServiceDto> {
        return client.from("services")
            .select(
                columns = Columns.raw(
                    """
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
                    providers (
                        name,
                        phone_number,
                        is_verified
                    )
                    """.trimIndent()
                )
            ) {
                filter {
                    eq("is_active", true)
                }
            }
            .decodeList<ServiceDto>()
    }

    suspend fun getServiceById(id: String): ServiceDto? {
        return client.from("services")
            .select(
                columns = Columns.raw(
                    """
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
                    providers (
                        name,
                        phone_number,
                        is_verified
                    )
                    """.trimIndent()
                )
            ) {
                filter {
                    eq("id", id)
                    eq("is_active", true)
                }
            }
            .decodeList<ServiceDto>()
            .firstOrNull()
    }
}
