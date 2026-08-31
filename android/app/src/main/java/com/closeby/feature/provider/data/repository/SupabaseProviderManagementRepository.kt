package com.closeby.feature.provider.data.repository

import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.availability.domain.repository.AvailabilityRepository
import com.closeby.feature.provider.data.mapper.ManagedServiceMapper
import com.closeby.feature.provider.data.mapper.ProviderProfileMapper
import com.closeby.feature.provider.data.remote.ProviderManagementRemoteDataSource
import com.closeby.feature.provider.domain.model.ManagedService
import com.closeby.feature.provider.domain.model.ManagedServiceSummary
import com.closeby.feature.provider.domain.model.ProviderProfile
import com.closeby.feature.provider.domain.model.ProviderProfileUpdate
import com.closeby.feature.provider.domain.model.ServiceFormInput
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.app.data.model.ProviderInsertDto
import com.closeby.app.data.model.ProviderUpdateDto
import com.closeby.feature.nearby.model.Coordinates
import com.closeby.feature.nearby.util.DistanceCalculator
import com.closeby.feature.nearby.util.DistanceFormatter

class SupabaseProviderManagementRepository(
    private val remote: ProviderManagementRemoteDataSource = ProviderManagementRemoteDataSource(),
    private val availabilityRepository: AvailabilityRepository
) : ProviderManagementRepository {

    override suspend fun getProviderProfile(
        providerId: String,
        viewerLatitude: Double?,
        viewerLongitude: Double?,
        isOwnProfile: Boolean
    ): Result<ProviderProfile> =
        runCatching {
            val provider = remote.getProviderById(providerId)
                ?: throw NoSuchElementException("Provider not found.")
            val services = remote.getServicesByProvider(providerId)
                .filter { it.deletedAt == null && it.isActive }
                .mapNotNull(ManagedServiceMapper::toSummary)
            val availability = availabilityRepository.getProviderAvailability(providerId)
                .getOrElse { emptyList() }
            val distanceLabel = if (viewerLatitude != null && viewerLongitude != null) {
                val meters = DistanceCalculator.distanceMeters(
                    Coordinates(viewerLatitude, viewerLongitude),
                    Coordinates(provider.latitude, provider.longitude)
                )
                DistanceFormatter.formatWithSuffix(meters)
            } else {
                null
            }
            ProviderProfileMapper.toProfile(
                provider = provider,
                services = services,
                availability = availability,
                distanceLabel = distanceLabel,
                isOwnProfile = isOwnProfile
            )?.copy(phoneNumber = if (isOwnProfile) provider.phoneNumber else null)
                ?: throw IllegalStateException("Invalid provider data.")
        }

    override suspend fun updateProviderProfile(
        providerId: String,
        update: ProviderProfileUpdate
    ): Result<ProviderProfile> = runCatching {
        val updated = remote.updateProvider(
            providerId,
            ProviderUpdateDto(
                name = update.name.trim(),
                phoneNumber = update.phoneNumber.trim(),
                profileImageUrl = update.profileImageUrl
            )
        )
        val services = remote.getServicesByProvider(providerId)
            .mapNotNull(ManagedServiceMapper::toSummary)
        val availability = availabilityRepository.getProviderAvailability(providerId)
            .getOrElse { emptyList() }
        ProviderProfileMapper.toProfile(
            provider = updated,
            services = services,
            availability = availability,
            distanceLabel = null,
            isOwnProfile = true
        )?.copy(phoneNumber = updated.phoneNumber)
            ?: throw IllegalStateException("Invalid provider data.")
    }

    override suspend fun getMyServices(providerId: String): Result<List<ManagedService>> =
        runCatching {
            remote.getServicesByProvider(providerId)
                .mapNotNull(ManagedServiceMapper::toDomain)
        }

    override suspend fun getManagedService(serviceId: String, providerId: String): Result<ManagedService> =
        runCatching {
            val dto = remote.getServiceById(serviceId)
                ?: throw NoSuchElementException("Service not found.")
            if (dto.providerId != providerId) {
                throw SecurityException("You can only edit your own services.")
            }
            ManagedServiceMapper.toDomain(dto)
                ?: throw IllegalStateException("Service has invalid data.")
        }

    override suspend fun createService(
        providerId: String,
        input: ServiceFormInput
    ): Result<ManagedService> = runCatching {
        val dto = remote.insertService(ManagedServiceMapper.toInsertDto(providerId, input))
        ManagedServiceMapper.toDomain(dto)
            ?: throw IllegalStateException("Created service has invalid data.")
    }

    override suspend fun updateService(
        serviceId: String,
        providerId: String,
        input: ServiceFormInput
    ): Result<ManagedService> = runCatching {
        val existing = remote.getServiceById(serviceId)
            ?: throw NoSuchElementException("Service not found.")
        if (existing.providerId != providerId) {
            throw SecurityException("You can only edit your own services.")
        }
        val dto = remote.updateService(serviceId, ManagedServiceMapper.toUpdateDto(input))
        ManagedServiceMapper.toDomain(dto)
            ?: throw IllegalStateException("Updated service has invalid data.")
    }

    override suspend fun setServiceActive(
        serviceId: String,
        providerId: String,
        isActive: Boolean
    ): Result<Unit> = runCatching {
        assertOwnership(serviceId, providerId)
        remote.setServiceActive(serviceId, isActive)
    }

    override suspend fun deleteService(serviceId: String, providerId: String): Result<Unit> =
        runCatching {
            assertOwnership(serviceId, providerId)
            remote.softDeleteService(serviceId)
        }

    override suspend fun getProviderIdForUser(userId: String): Result<String?> =
        runCatching { remote.getProviderByUserId(userId)?.id }

    override suspend fun ensureProviderForUser(
        userId: String,
        email: String,
        defaultName: String
    ): Result<String> = runCatching {
        remote.getProviderByUserId(userId)?.id ?: run {
            val created = remote.insertProvider(
                ProviderInsertDto(
                    name = defaultName.ifBlank { email.substringBefore("@") },
                    category = ServiceCategory.EQUIPMENT.name,
                    phoneNumber = "",
                    latitude = 12.9716,
                    longitude = 77.5946,
                    userId = userId
                )
            )
            created.id
        }
    }

    private suspend fun assertOwnership(serviceId: String, providerId: String) {
        val existing = remote.getServiceById(serviceId)
            ?: throw NoSuchElementException("Service not found.")
        if (existing.providerId != providerId) {
            throw SecurityException("You can only manage your own services.")
        }
    }
}
