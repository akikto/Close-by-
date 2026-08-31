package com.closeby.request.data.repository

import com.closeby.request.data.mapper.ServiceRequestMapper
import com.closeby.request.data.remote.ServiceRequestRemoteDataSource
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.repository.ServiceRequestRepository

class SupabaseServiceRequestRepository(
    private val remote: ServiceRequestRemoteDataSource = ServiceRequestRemoteDataSource()
) : ServiceRequestRepository {

    override suspend fun createRequest(request: ServiceRequest): Result<ServiceRequest> =
        runCatching {
            val dto = remote.insert(ServiceRequestMapper.toInsertDto(request))
            ServiceRequestMapper.toDomain(dto)
                ?: throw IllegalStateException("Created request has invalid data.")
        }

    override suspend fun getCustomerRequests(customerId: String?): Result<List<ServiceRequest>> =
        runCatching {
            remote.getByCustomer(customerId).mapNotNull(ServiceRequestMapper::toDomain)
        }

    override suspend fun getProviderRequests(providerId: String): Result<List<ServiceRequest>> =
        runCatching {
            remote.getByProvider(providerId).mapNotNull(ServiceRequestMapper::toDomain)
        }

    override suspend fun acceptRequest(requestId: String, providerId: String): Result<ServiceRequest> =
        updateForProvider(requestId, providerId, ServiceRequestStatus.ACCEPTED)

    override suspend fun rejectRequest(requestId: String, providerId: String): Result<ServiceRequest> =
        updateForProvider(requestId, providerId, ServiceRequestStatus.REJECTED)

    override suspend fun completeRequest(requestId: String): Result<ServiceRequest> =
        runCatching {
            val existing = remote.getById(requestId)
                ?: throw NoSuchElementException("Request not found.")
            if (!existing.status.let { it == ServiceRequestStatus.ACCEPTED.name }) {
                val current = ServiceRequestStatus.valueOf(existing.status)
                if (!current.canTransitionTo(ServiceRequestStatus.COMPLETED)) {
                    throw IllegalStateException("Cannot complete request in status ${existing.status}.")
                }
            }
            val dto = remote.updateStatus(requestId, ServiceRequestStatus.COMPLETED.name)
            ServiceRequestMapper.toDomain(dto)
                ?: throw IllegalStateException("Updated request has invalid data.")
        }

    override suspend fun cancelRequest(requestId: String): Result<ServiceRequest> =
        runCatching {
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
        if (!current.canTransitionTo(status)) {
            throw IllegalStateException("Cannot transition $current -> $status")
        }
        val dto = remote.updateStatus(requestId, status.name)
        ServiceRequestMapper.toDomain(dto)
            ?: throw IllegalStateException("Updated request has invalid data.")
    }
}
