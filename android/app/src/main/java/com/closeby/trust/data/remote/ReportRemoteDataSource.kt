package com.closeby.trust.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.trust.data.model.ReportDto
import com.closeby.trust.data.model.ReportInsertDto
import io.github.jan.supabase.postgrest.from

class ReportRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun insert(dto: ReportInsertDto): ReportDto =
        client.from("reports")
            .insert(dto) { select() }
            .decodeSingle<ReportDto>()

    suspend fun getByReporter(reporterId: String): List<ReportDto> =
        client.from("reports")
            .select { filter { eq("reporter_id", reporterId) } }
            .decodeList<ReportDto>()
}
