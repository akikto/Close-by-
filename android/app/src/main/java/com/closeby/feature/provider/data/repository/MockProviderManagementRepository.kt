package com.closeby.feature.provider.data.repository

import com.closeby.availability.domain.model.ProviderAvailability
import com.closeby.feature.provider.domain.model.ManagedService
import com.closeby.feature.provider.domain.model.ProviderOnboardingInput
import com.closeby.feature.provider.domain.model.ProviderProfile
import com.closeby.feature.provider.domain.model.ProviderProfileUpdate
import com.closeby.feature.provider.domain.model.ServiceFormInput
import com.closeby.feature.provider.domain.repository.ProviderManagementRepository
import com.closeby.feature.servicelisting.domain.model.AvailabilityStatus
import com.closeby.feature.servicelisting.domain.model.PriceInfo
import com.closeby.feature.servicelisting.domain.model.PriceUnit
import com.closeby.feature.servicelisting.domain.model.ServiceCategory
import com.closeby.feature.servicelisting.domain.model.ServiceSubcategory
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory provider management for local development without Supabase auth.
 */
class MockProviderManagementRepository : ProviderManagementRepository {

    private val demoProviderId = "11111111-1111-1111-1111-111111111101"
    private val userToProvider = ConcurrentHashMap<String, String>()
    private val services = ConcurrentHashMap<String, ManagedService>()

    init {
        services["22222222-2222-2222-2222-222222222201"] = ManagedService(
            id = "22222222-2222-2222-2222-222222222201",
            providerId = demoProviderId,
            category = ServiceCategory.EQUIPMENT,
            subcategory = ServiceSubcategory.WATER_PUMP,
            title = "Water Pump",
            description = "High-capacity water pump.",
            imageUrls = emptyList(),
            latitude = 12.9716,
            longitude = 77.5946,
            availability = AvailabilityStatus.AVAILABLE_NOW,
            price = PriceInfo(500.0, PriceUnit.DAY),
            contactNumber = "+910000000001",
            isActive = true,
            isDeleted = false,
            rating = 4.7,
            reviewCount = 32
        )
    }

    override suspend fun getProviderProfile(
        providerId: String,
        viewerLatitude: Double?,
        viewerLongitude: Double?,
        isOwnProfile: Boolean
    ): Result<ProviderProfile> =
        runCatching {
            if (providerId != demoProviderId) throw NoSuchElementException("Provider not found.")
            ProviderProfile(
                id = demoProviderId,
                name = "Ravi Kumar",
                profileImageUrl = null,
                category = ServiceCategory.EQUIPMENT,
                isVerified = true,
                rating = 4.7,
                reviewCount = 32,
                phoneNumber = null,
                distanceLabel = "2.4 km away",
                services = services.values
                    .filter { it.providerId == providerId && !it.isDeleted }
                    .map {
                        com.closeby.feature.provider.domain.model.ManagedServiceSummary(
                            id = it.id,
                            title = it.title,
                            category = it.category,
                            subcategory = it.subcategory,
                            price = it.price,
                            availability = it.availability,
                            isActive = it.isActive
                        )
                    },
                availability = defaultAvailability(providerId),
                isOwnProfile = isOwnProfile
            ).let { profile ->
                if (isOwnProfile) profile.copy(phoneNumber = "+910000000001") else profile
            }
        }

    override suspend fun updateProviderProfile(
        providerId: String,
        update: ProviderProfileUpdate
    ): Result<ProviderProfile> = getProviderProfile(providerId, null, null, true).map { profile ->
        profile.copy(
            name = update.name,
            phoneNumber = update.phoneNumber,
            profileImageUrl = update.profileImageUrl,
            isOwnProfile = true
        )
    }

    override suspend fun getMyServices(providerId: String): Result<List<ManagedService>> =
        Result.success(
            services.values.filter { it.providerId == providerId && !it.isDeleted }
        )

    override suspend fun getManagedService(serviceId: String, providerId: String): Result<ManagedService> =
        runCatching {
            val service = services[serviceId] ?: throw NoSuchElementException("Service not found.")
            if (service.providerId != providerId) throw SecurityException("Not your service.")
            service
        }

    override suspend fun createService(providerId: String, input: ServiceFormInput): Result<ManagedService> =
        runCatching {
            val id = UUID.randomUUID().toString()
            val created = ManagedService(
                id = id,
                providerId = providerId,
                category = input.category,
                subcategory = input.subcategory,
                title = input.title.trim(),
                description = input.description.trim(),
                imageUrls = input.imageUrls,
                latitude = input.latitude,
                longitude = input.longitude,
                availability = input.availability,
                price = input.priceAmount?.let {
                    PriceInfo(it, input.priceUnit ?: PriceUnit.NONE, input.priceIsStarting)
                },
                contactNumber = input.contactNumber.trim(),
                isActive = true,
                isDeleted = false
            )
            services[id] = created
            created
        }

    override suspend fun updateService(
        serviceId: String,
        providerId: String,
        input: ServiceFormInput
    ): Result<ManagedService> = runCatching {
        val existing = services[serviceId] ?: throw NoSuchElementException("Service not found.")
        if (existing.providerId != providerId) throw SecurityException("Not your service.")
        val updated = existing.copy(
            category = input.category,
            subcategory = input.subcategory,
            title = input.title.trim(),
            description = input.description.trim(),
            imageUrls = input.imageUrls,
            latitude = input.latitude,
            longitude = input.longitude,
            availability = input.availability,
            price = input.priceAmount?.let {
                PriceInfo(it, input.priceUnit ?: PriceUnit.NONE, input.priceIsStarting)
            },
            contactNumber = input.contactNumber.trim()
        )
        services[serviceId] = updated
        updated
    }

    override suspend fun setServiceActive(
        serviceId: String,
        providerId: String,
        isActive: Boolean
    ): Result<Unit> = runCatching {
        val existing = services[serviceId] ?: throw NoSuchElementException("Service not found.")
        if (existing.providerId != providerId) throw SecurityException("Not your service.")
        services[serviceId] = existing.copy(isActive = isActive)
    }

    override suspend fun deleteService(serviceId: String, providerId: String): Result<Unit> =
        runCatching {
            val existing = services[serviceId] ?: throw NoSuchElementException("Service not found.")
            if (existing.providerId != providerId) throw SecurityException("Not your service.")
            services[serviceId] = existing.copy(isActive = false, isDeleted = true)
        }

    override suspend fun getProviderIdForUser(userId: String): Result<String?> =
        Result.success(userToProvider[userId])

    override suspend fun createProviderProfile(
        userId: String,
        input: ProviderOnboardingInput
    ): Result<String> = runCatching {
        userToProvider[userId] ?: run {
            val id = UUID.randomUUID().toString()
            userToProvider[userId] = id
            id
        }
    }

    override suspend fun ensureProviderForUser(
        userId: String,
        email: String,
        defaultName: String
    ): Result<String> = runCatching {
        userToProvider[userId]
            ?: throw IllegalStateException("No provider profile exists. Complete provider onboarding first.")
    }

    private fun defaultAvailability(providerId: String): List<ProviderAvailability> =
        DayOfWeek.entries.map { day ->
            val weekend = day == DayOfWeek.SUNDAY
            ProviderAvailability(
                providerId = providerId,
                dayOfWeek = day,
                isAvailable = !weekend,
                startTime = if (weekend) null else LocalTime.of(8, 0),
                endTime = if (weekend) null else LocalTime.of(18, 0)
            )
        }
}
