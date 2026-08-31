package com.closeby.notification.domain.push

/**
 * Abstraction for FCM push delivery. The app works fully without push.
 */
interface PushNotificationGateway {
    suspend fun registerDeviceToken(userId: String, token: String): Result<Unit>
    suspend fun unregisterDeviceToken(userId: String, token: String): Result<Unit>
    fun isConfigured(): Boolean
}

class NoOpPushNotificationGateway : PushNotificationGateway {
    override suspend fun registerDeviceToken(userId: String, token: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun unregisterDeviceToken(userId: String, token: String): Result<Unit> =
        Result.success(Unit)

    override fun isConfigured(): Boolean = false
}
