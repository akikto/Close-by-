package com.closeby.notification.data.remote

import com.closeby.app.core.network.SupabaseClientProvider
import com.closeby.notification.data.model.NotificationDto
import com.closeby.notification.data.model.NotificationInsertDto
import com.closeby.notification.data.model.NotificationReadUpdateDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class NotificationRemoteDataSource(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {

    suspend fun getByUser(userId: String): List<NotificationDto> =
        client.from("notifications")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<NotificationDto>()

    suspend fun countUnread(userId: String): Int =
        client.from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
            .decodeList<NotificationDto>()
            .size

    suspend fun insert(dto: NotificationInsertDto): NotificationDto =
        client.from("notifications")
            .insert(dto) { select() }
            .decodeSingle<NotificationDto>()

    suspend fun markRead(id: String) {
        client.from("notifications")
            .update(NotificationReadUpdateDto()) {
                filter { eq("id", id) }
            }
    }

    suspend fun markAllRead(userId: String) {
        client.from("notifications")
            .update(NotificationReadUpdateDto()) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
    }
}
