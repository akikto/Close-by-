package com.closeby.trust.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.trust.data.model.ReviewDto
import com.closeby.trust.data.model.ReviewInsertDto
import io.github.jan.supabase.postgrest.from

class ReviewRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun insert(dto: ReviewInsertDto): ReviewDto =
        client.from("reviews")
            .insert(dto) { select() }
            .decodeSingle<ReviewDto>()

    suspend fun getByProvider(providerId: String): List<ReviewDto> =
        client.from("reviews")
            .select {
                filter {
                    eq("provider_id", providerId)
                    eq("is_visible", true)
                    eq("moderation_status", "VISIBLE")
                }
            }
            .decodeList<ReviewDto>()

    suspend fun existsForRequest(
        requestId: String,
        reviewerId: String,
        reviewerRole: String
    ): Boolean =
        client.from("reviews")
            .select {
                filter {
                    eq("request_id", requestId)
                    eq("reviewer_id", reviewerId)
                    eq("reviewer_role", reviewerRole)
                }
            }
            .decodeList<ReviewDto>()
            .isNotEmpty()
}
