package com.closeby.app.core.di

import android.content.Context
import com.closeby.app.BuildConfig
import com.closeby.notification.data.repository.MockNotificationRepository
import com.closeby.notification.data.repository.SupabaseNotificationRepository
import com.closeby.notification.domain.handler.NotificationEventHandler
import com.closeby.notification.domain.repository.NotificationRepository
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.notification.domain.push.NoOpPushNotificationGateway
import com.closeby.notification.domain.push.PushNotificationGateway

/**
 * Notification module wiring. FCM is not configured — in-app notifications only.
 */
object NotificationDependenciesFactory {

    private val hasSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private val mockRepository: MockNotificationRepository by lazy { MockNotificationRepository() }

    private var repository: NotificationRepository? = null
    private var eventHandler: NotificationEventHandler? = null
    private val pushGateway: PushNotificationGateway = NoOpPushNotificationGateway()

    fun pushNotificationGateway(): PushNotificationGateway = pushGateway

    fun notificationRepository(): NotificationRepository {
        repository?.let { return it }
        val created: NotificationRepository = if (hasSupabase) {
            SupabaseNotificationRepository()
        } else {
            mockRepository.also { repo ->
                runBlocking {
                    val session = ProviderDependenciesFactory.authRepository().getCurrentSession()
                    session?.userId?.let { repo.seedSample(it) }
                }
            }
        }
        repository = created
        return created
    }

    fun ensureEventHandlerStarted(context: Context) {
        if (eventHandler != null) return
        val serviceRequestRepository: ServiceRequestRepository =
            ProviderDependenciesFactory.serviceRequestRepository(context)
        val providerResolver = if (hasSupabase) {
            NotificationEventHandler.defaultProviderUserResolver()
        } else {
            val demoUserId = runBlocking {
                ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId
                    ?: "demo-notifications-user"
            }
            NotificationEventHandler.mockProviderUserResolver(demoUserId)
        }
        eventHandler = NotificationEventHandler(
            notificationRepository = notificationRepository(),
            serviceRequestRepository = serviceRequestRepository,
            resolveProviderUserId = providerResolver
        ).also { it.start() }
    }
}
