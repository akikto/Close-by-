package com.closeby.trust.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.trust.data.model.VerificationInsertDto
import com.closeby.trust.data.model.VerificationSubmissionDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class VerificationRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun insert(dto: VerificationInsertDto): VerificationSubmissionDto =
        client.from("provider_verification_submissions")
            .insert(dto) { select() }
            .decodeSingle<VerificationSubmissionDto>()

    suspend fun getLatestByProvider(providerId: String): VerificationSubmissionDto? =
        client.from("provider_verification_submissions")
            .select {
                filter { eq("provider_id", providerId) }
                order("created_at", Order.DESCENDING)
                limit(1)
            }
            .decodeList<VerificationSubmissionDto>()
            .firstOrNull()
}
