package com.closeby.request.data.mock

import com.closeby.request.domain.model.BudgetUnit
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.notification.RequestNotificationBridge
import com.closeby.request.domain.notification.RequestNotificationEvent
import com.closeby.request.domain.repository.ServiceRequestRepository
import com.closeby.request.domain.validation.ServiceRequestValidator
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory fallback when Supabase credentials are not configured.
 * Enforces status transitions and basic ownership for local development.
 */
class InMemoryServiceRequestRepository : ServiceRequestRepository {

    private val requests = CopyOnWriteArrayList(sampleRequests())

    override suspend fun createRequest(request: ServiceRequest): Result<ServiceRequest> {
        requests.add(request)
        RequestNotificationBridge.publish(RequestNotificationEvent.NewProviderRequest(request.id))
        return Result.success(request)
    }

    override suspend fun getCustomerRequests(
        customerId: String?,
        clientSessionId: String?
    ): Result<List<ServiceRequest>> = Result.success(
        requests.filter { request ->
            (customerId != null && request.customerId == customerId) ||
                (!clientSessionId.isNullOrBlank() && request.clientSessionId == clientSessionId)
        }
    )

    override suspend fun getProviderRequests(providerId: String): Result<List<ServiceRequest>> =
        Result.success(requests.filter { it.providerId == providerId })

    override suspend fun getRequestById(requestId: String): Result<ServiceRequest> {
        val found = requests.firstOrNull { it.id == requestId }
            ?: return Result.failure(NoSuchElementException("Request not found."))
        return Result.success(found)
    }

    override suspend fun acceptRequest(requestId: String, providerId: String): Result<ServiceRequest> =
        updateForProvider(requestId, providerId, ServiceRequestStatus.ACCEPTED)

    override suspend fun rejectRequest(requestId: String, providerId: String): Result<ServiceRequest> =
        updateForProvider(requestId, providerId, ServiceRequestStatus.REJECTED)

    override suspend fun completeRequest(requestId: String, providerId: String): Result<ServiceRequest> =
        updateForProvider(requestId, providerId, ServiceRequestStatus.COMPLETED)

    override suspend fun cancelRequest(
        requestId: String,
        customerId: String?,
        clientSessionId: String?
    ): Result<ServiceRequest> = runCatching {
        val index = requests.indexOfFirst { it.id == requestId }
        if (index == -1) throw NoSuchElementException("Request not found.")
        val current = requests[index]
        val owns = (customerId != null && current.customerId == customerId) ||
            (!clientSessionId.isNullOrBlank() && current.clientSessionId == clientSessionId)
        if (!owns) throw SecurityException("Not your request.")
        if (!current.status.canTransitionTo(ServiceRequestStatus.CANCELLED)) {
            throw IllegalStateException("Cannot cancel in status ${current.status}.")
        }
        val updated = current.copy(status = ServiceRequestStatus.CANCELLED, updatedAt = System.currentTimeMillis())
        requests[index] = updated
        updated
    }

    private fun updateForProvider(
        requestId: String,
        providerId: String,
        status: ServiceRequestStatus
    ): Result<ServiceRequest> = runCatching {
        val index = requests.indexOfFirst { it.id == requestId }
        if (index == -1) throw NoSuchElementException("Request not found.")
        val current = requests[index]
        if (current.providerId != providerId) throw SecurityException("Not your request.")
        ServiceRequestValidator.validateStatusTransition(current.status, status).getOrThrow()
        val updated = current.copy(status = status, updatedAt = System.currentTimeMillis())
        requests[index] = updated
        when (status) {
            ServiceRequestStatus.ACCEPTED ->
                RequestNotificationBridge.publish(RequestNotificationEvent.RequestAccepted(requestId))
            ServiceRequestStatus.REJECTED ->
                RequestNotificationBridge.publish(RequestNotificationEvent.RequestRejected(requestId))
            ServiceRequestStatus.COMPLETED ->
                RequestNotificationBridge.publish(RequestNotificationEvent.RequestCompleted(requestId))
            else -> Unit
        }
        updated
    }

    private fun sampleRequests(): List<ServiceRequest> {
        val now = System.currentTimeMillis()
        val session = "demo-session"
        return listOf(
            ServiceRequest(
                id = "req_001",
                serviceId = "22222222-2222-2222-2222-222222222201",
                providerId = "11111111-1111-1111-1111-111111111101",
                customerId = null,
                customerName = "Anita",
                customerPhone = "+919876543210",
                serviceTitle = "Water Pump Rental",
                requestedDate = LocalDate.now().plusDays(1),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(12, 0),
                duration = "3 hours",
                budgetAmount = 900.0,
                budgetUnit = BudgetUnit.DAY,
                clientSessionId = session,
                providerName = "Ravi Kumar",
                providerPhone = "+910000000001",
                status = ServiceRequestStatus.PENDING,
                createdAt = now,
                updatedAt = now
            ),
            ServiceRequest(
                id = "req_002",
                serviceId = "22222222-2222-2222-2222-222222222202",
                providerId = "11111111-1111-1111-1111-111111111101",
                customerId = null,
                customerName = "Rahul",
                customerPhone = "+919123456789",
                serviceTitle = "House Cleaning Labour",
                requestedDate = LocalDate.now(),
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(16, 0),
                duration = "2 hours",
                budgetAmount = 500.0,
                budgetUnit = BudgetUnit.JOB,
                clientSessionId = session,
                providerName = "Suresh Electricals",
                providerPhone = "+910000000002",
                status = ServiceRequestStatus.ACCEPTED,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
