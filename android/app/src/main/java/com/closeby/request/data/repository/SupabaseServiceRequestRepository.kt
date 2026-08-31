package com.closeby.request.data.repository

import com.closeby.app.core.session.ClientSessionStorage
import com.closeby.availability.domain.repository.AvailabilityRepository
import com.closeby.feature.servicelisting.domain.repository.ServiceRepository
import com.closeby.request.data.mapper.ServiceRequestMapper
import com.closeby.request.data.remote.ServiceRequestRemoteDataSource
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.notification.RequestNotificationBridge
import com.closeby.request.domain.notification.RequestNotificationEvent
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.request.domain.validation.RequestAvailabilityChecker
import com.closeby.request.domain.validation.ServiceRequestValidationError
import com.closeby.request.domain.validation.ServiceRequestValidator

class SupabaseServiceRequestRepository(
    private val remote: ServiceRequestRemoteDataSource = ServiceRequestRemoteDataSource(),
    private val serviceRepository: ServiceRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val clientSessionStorage: ClientSessionStorage? = null
) : ServiceRequestRepository {

    override suspend fun createRequest(request: ServiceRequest): Result<ServiceRequest> =
        runCatching {
            serviceRepository.getServiceById(request.serviceId)
                .getOrElse { throw ServiceRequestValidationError.ServiceInactive }
            RequestAvailabilityChecker.validateProviderAvailable(
                availabilityRepository,
                request.providerId,
                request.requestedDate,
                request.startTime,
                request.endTime
            ).getOrThrow()

            val dto = remote.insert(ServiceRequestMapper.toInsertDto(request))
            val created = ServiceRequestMapper.toDomain(dto)
                ?: throw IllegalStateException("Created request has invalid data.")
            clientSessionStorage?.rememberRequestId(created.id)
            clientSessionStorage?.cacheRequest(created)
            RequestNotificationBridge.publish(RequestNotificationEvent.NewProviderRequest(created.id))
            created
        }

    override suspend fun getCustomerRequests(
        customerId: String?,
        clientSessionId: String?
    ): Result<List<ServiceRequest>> = runCatching {
        val fromAuth = if (!customerId.isNullOrBlank()) {
            remote.getByCustomerId(customerId)
        } else {
            emptyList()
        }

        val fromIds = if (customerId.isNullOrBlank()) {
            val remembered = clientSessionStorage?.getRememberedRequestIds().orEmpty()
            if (remembered.isEmpty()) {
                emptyList()
            } else {
                try {
                    remote.getByIds(remembered.toList())
                } catch (_: Exception) {
                    emptyList()
                }
            }
        } else {
            emptyList()
        }

        val cached = if (customerId.isNullOrBlank()) {
            clientSessionStorage?.getCachedRequests().orEmpty()
        } else {
            emptyList()
        }

        (fromAuth.mapNotNull(ServiceRequestMapper::toDomain) +
            fromIds.mapNotNull(ServiceRequestMapper::toDomain) +
            cached)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getProviderRequests(providerId: String): Result<List<ServiceRequest>> =
        runCatching {
            remote.getByProvider(providerId).mapNotNull(ServiceRequestMapper::toDomain)
        }

    override suspend fun getRequestById(requestId: String): Result<ServiceRequest> =
        runCatching {
            val dto = remote.getById(requestId)
            if (dto != null) {
                ServiceRequestMapper.toDomain(dto)
                    ?: throw IllegalStateException("Request has invalid data.")
            } else {
                clientSessionStorage?.getCachedRequests()
                    ?.firstOrNull { it.id == requestId }
                    ?: throw NoSuchElementException("Request not found.")
            }
        }

    override suspend fun acceptRequest(requestId: String, providerId: String): Result<ServiceRequest> {
        val result = updateForProvider(requestId, providerId, ServiceRequestStatus.ACCEPTED)
        result.onSuccess { RequestNotificationBridge.publish(RequestNotificationEvent.RequestAccepted(it.id)) }
        return result
    }

    override suspend fun rejectRequest(requestId: String, providerId: String): Result<ServiceRequest> {
        val result = updateForProvider(requestId, providerId, ServiceRequestStatus.REJECTED)
        result.onSuccess { RequestNotificationBridge.publish(RequestNotificationEvent.RequestRejected(it.id)) }
        return result
    }

    override suspend fun completeRequest(requestId: String, providerId: String): Result<ServiceRequest> {
        val result = updateForProvider(requestId, providerId, ServiceRequestStatus.COMPLETED)
        result.onSuccess { RequestNotificationBridge.publish(RequestNotificationEvent.RequestCompleted(it.id)) }
        return result
    }

    override suspend fun cancelRequest(
        requestId: String,
        customerId: String?,
        clientSessionId: String?
    ): Result<ServiceRequest> = runCatching {
        val rememberedIds = clientSessionStorage?.getRememberedRequestIds().orEmpty()
        val remoteDto = remote.getById(requestId)
        if (remoteDto == null) {
            val cached = clientSessionStorage?.getCachedRequests()
                ?.firstOrNull { it.id == requestId }
            if (cached != null) {
                return@runCatching cancelAnonymousCachedRequest(
                    cached,
                    customerId,
                    clientSessionId,
                    rememberedIds
                )
            }
            throw NoSuchElementException("Request not found.")
        }
        assertCustomerOwnership(remoteDto, customerId, clientSessionId, rememberedIds)
        val current = ServiceRequestStatus.valueOf(remoteDto.status)
        if (!current.canTransitionTo(ServiceRequestStatus.CANCELLED)) {
            throw IllegalStateException("Cannot cancel request in status $current.")
        }
        if (customerId.isNullOrBlank()) {
            throw SecurityException(
                "Sign in with Email OTP to cancel requests securely on the server."
            )
        }
        val dto = remote.updateStatus(requestId, ServiceRequestStatus.CANCELLED.name)
        ServiceRequestMapper.toDomain(dto)
            ?: throw IllegalStateException("Updated request has invalid data.")
    }.onSuccess { RequestNotificationBridge.publish(RequestNotificationEvent.RequestCancelled(it.id)) }

    private suspend fun cancelAnonymousCachedRequest(
        cached: ServiceRequest,
        customerId: String?,
        clientSessionId: String?,
        rememberedRequestIds: Set<String>
    ): ServiceRequest {
        val ownsBySession = !clientSessionId.isNullOrBlank() &&
            cached.clientSessionId == clientSessionId
        val ownsByRemembered = rememberedRequestIds.contains(cached.id)
        if (!customerId.isNullOrBlank()) {
            throw SecurityException("Request not found on server.")
        }
        if (!ownsBySession && !ownsByRemembered) {
            throw SecurityException("You can only modify your own requests.")
        }
        if (!cached.status.canTransitionTo(ServiceRequestStatus.CANCELLED)) {
            throw IllegalStateException("Cannot cancel request in status ${cached.status}.")
        }
        val updated = cached.copy(
            status = ServiceRequestStatus.CANCELLED,
            updatedAt = System.currentTimeMillis()
        )
        clientSessionStorage?.updateCachedRequest(updated)
        return updated
    }

    private suspend fun updateForProvider(
        requestId: String,
        providerId: String,
        status: ServiceRequestStatus
    ): Result<ServiceRequest> = runCatching {
        val existing = remote.getById(requestId)
            ?: throw NoSuchElementException("Request not found.")
        if (existing.providerId != providerId) {
            throw SecurityException("You can only manage your own requests.")
        }
        val current = ServiceRequestStatus.valueOf(existing.status)
        ServiceRequestValidator.validateStatusTransition(current, status).getOrThrow()
        val dto = remote.updateStatus(requestId, status.name)
        ServiceRequestMapper.toDomain(dto)
            ?: throw IllegalStateException("Updated request has invalid data.")
    }

    private fun assertCustomerOwnership(
        dto: com.closeby.request.data.model.ServiceRequestDto,
        customerId: String?,
        clientSessionId: String?,
        rememberedRequestIds: Set<String>
    ) {
        val ownsByAuth = !customerId.isNullOrBlank() && dto.customerId == customerId
        val ownsBySession = !clientSessionId.isNullOrBlank() &&
            dto.clientSessionId == clientSessionId
        val ownsByRemembered = rememberedRequestIds.contains(dto.id)
        if (!ownsByAuth && !ownsBySession && !ownsByRemembered) {
            throw SecurityException("You can only modify your own requests.")
        }
    }
}
