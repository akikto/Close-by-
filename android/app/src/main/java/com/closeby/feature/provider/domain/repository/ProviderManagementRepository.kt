package com.closeby.feature.provider.domain.repository

import com.closeby.feature.provider.domain.model.ManagedService
import com.closeby.feature.provider.domain.model.ProviderOnboardingInput
import com.closeby.feature.provider.domain.model.ProviderProfile
import com.closeby.feature.provider.domain.model.ProviderProfileUpdate
import com.closeby.feature.provider.domain.model.ServiceFormInput

interface ProviderManagementRepository {

    suspend fun getProviderProfile(
        providerId: String,
        viewerLatitude: Double? = null,
        viewerLongitude: Double? = null,
        isOwnProfile: Boolean = false
    ): Result<ProviderProfile>

    suspend fun updateProviderProfile(
        providerId: String,
        update: ProviderProfileUpdate
    ): Result<ProviderProfile>

    suspend fun getMyServices(providerId: String): Result<List<ManagedService>>

    suspend fun getManagedService(serviceId: String, providerId: String): Result<ManagedService>

    suspend fun createService(providerId: String, input: ServiceFormInput): Result<ManagedService>

    suspend fun updateService(
        serviceId: String,
        providerId: String,
        input: ServiceFormInput
    ): Result<ManagedService>

    suspend fun setServiceActive(
        serviceId: String,
        providerId: String,
        isActive: Boolean
    ): Result<Unit>

    suspend fun deleteService(serviceId: String, providerId: String): Result<Unit>

    suspend fun getProviderIdForUser(userId: String): Result<String?>

    /** Creates a provider profile only when the user explicitly opts in. */
    suspend fun createProviderProfile(
        userId: String,
        input: ProviderOnboardingInput
    ): Result<String>

    /**
     * Returns an existing provider id for the user. Does not create a profile.
     * @deprecated Prefer [getProviderIdForUser] or [createProviderProfile].
     */
    suspend fun ensureProviderForUser(
        userId: String,
        email: String,
        defaultName: String
    ): Result<String>
}
