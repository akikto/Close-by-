package com.closeby.request.data.mock

import com.closeby.request.domain.model.BudgetUnit
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import com.closeby.request.domain.repository.ServiceRequestRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.CopyOnWriteArrayList

/**
 * ⚠️ TEMPORARY MOCK — in-memory only, resets on process death, and does
 * NOT enforce authorization (any caller can accept/reject any request).
 * This exists only so the Requests tab is demoable before a real
 * Supabase-backed [ServiceRequestRepository] is wired in by the base
 * project (see Agent 5's INTEGRATION_NOTES.md — authorization MUST be
 * enforced server-side in the real implementation).
 */
class InMemoryServiceRequestRepository : ServiceRequestRepository {

    private val requests = CopyOnWriteArrayList(sampleRequests())

    override suspend fun createRequest(request: ServiceRequest): Result<ServiceRequest> {
        requests.add(request)
        return Result.success(request)
    }

    override suspend fun getCustomerRequests(customerId: String?): Result<List<ServiceRequest>> =
        Result.success(requests.filter { it.customerId == customerId })

    override suspend fun getProviderRequests(providerId: String): Result<List<ServiceRequest>> =
        Result.success(requests.filter { it.providerId == providerId })

    override suspend fun acceptRequest(requestId: String, providerId: String): Result<ServiceRequest> =
        updateStatus(requestId, ServiceRequestStatus.ACCEPTED)

    override suspend fun rejectRequest(requestId: String, providerId: String): Result<ServiceRequest> =
        updateStatus(requestId, ServiceRequestStatus.REJECTED)

    override suspend fun completeRequest(requestId: String): Result<ServiceRequest> =
        updateStatus(requestId, ServiceRequestStatus.COMPLETED)

    override suspend fun cancelRequest(requestId: String): Result<ServiceRequest> =
        updateStatus(requestId, ServiceRequestStatus.CANCELLED)

    private fun updateStatus(requestId: String, status: ServiceRequestStatus): Result<ServiceRequest> {
        val index = requests.indexOfFirst { it.id == requestId }
        if (index == -1) return Result.failure(NoSuchElementException("No request found for id=$requestId"))
        val current = requests[index]
        if (!current.status.canTransitionTo(status)) {
            return Result.failure(IllegalStateException("Cannot transition ${current.status} -> $status"))
        }
        val updated = current.copy(status = status, updatedAt = System.currentTimeMillis())
        requests[index] = updated
        return Result.success(updated)
    }

    private fun sampleRequests(): List<ServiceRequest> {
        val now = System.currentTimeMillis()
        return listOf(
            ServiceRequest(
                id = "req_001",
                serviceId = "svc_001",
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
                status = ServiceRequestStatus.PENDING,
                createdAt = now,
                updatedAt = now
            ),
            ServiceRequest(
                id = "req_002",
                serviceId = "svc_002",
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
                status = ServiceRequestStatus.ACCEPTED,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
