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
            RequestNotificationBridge.publish(RequestNotificationEvent.NewProviderRequest(created.id))
            created
        }

    override suspend fun getCustomerRequests(
        customerId: String?,
        clientSessionId: String?
    ): Result<List<ServiceRequest>> = runCatching {
        val fromAuth = if (customerId != null) {
            remote.getByCustomerId(customerId)
        } else {
            emptyList()
        }
        val fromSession = if (!clientSessionId.isNullOrBlank()) {
            remote.getByClientSession(clientSessionId)
        } else {
            emptyList()
        }
        val remembered = clientSessionStorage?.getRememberedRequestIds().orEmpty()
        val fromIds = remote.getByIds(remembered.toList())
        (fromAuth + fromSession + fromIds)
            .distinctBy { it.id }
            .mapNotNull(ServiceRequestMapper::toDomain)
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getProviderRequests(providerId: String): Result<List<ServiceRequest>> =
        runCatching {
            remote.getByProvider(providerId).mapNotNull(ServiceRequestMapper::toDomain)
        }

    override suspend fun getRequestById(requestId: String): Result<ServiceRequest> =
        runCatching {
            val dto = remote.getById(requestId)
                ?: throw NoSuchElementException("Request not found.")
            ServiceRequestMapper.toDomain(dto)
                ?: throw IllegalStateException("Request has invalid data.")
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
        val existing = remote.getById(requestId)
            ?: throw NoSuchElementException("Request not found.")
        assertCustomerOwnership(existing, customerId, clientSessionId)
        val current = ServiceRequestStatus.valueOf(existing.status)
        if (!current.canTransitionTo(ServiceRequestStatus.CANCELLED)) {
            throw IllegalStateException("Cannot cancel request in status $current.")
        }
        val dto = remote.updateStatus(requestId, ServiceRequestStatus.CANCELLED.name)
        ServiceRequestMapper.toDomain(dto)
            ?: throw IllegalStateException("Updated request has invalid data.")
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
        clientSessionId: String?
    ) {
        val ownsByAuth = customerId != null && dto.customerId == customerId
        val ownsBySession = !clientSessionId.isNullOrBlank() &&
            dto.clientSessionId == clientSessionId
        if (!ownsByAuth && !ownsBySession) {
            throw SecurityException("You can only modify your own requests.")
        }
    }
}
