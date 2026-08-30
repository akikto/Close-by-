package com.closeby.request.domain.repository

import com.closeby.request.domain.model.ServiceRequest

/**
 * Backend-agnostic repository contract for service requests.
 *
 * No Firebase, no new database — implementations connect to whatever
 * backend the main project already uses (e.g. Supabase/PostgreSQL).
 *
 * Implementations MUST enforce authorization server-side:
 *  - a provider must not accept/reject/complete another provider's request
 *  - a customer must not modify another customer's request
 * UI-level checks alone are not sufficient (see SECURITY in the contract).
 */
interface ServiceRequestRepository {

    suspend fun createRequest(request: ServiceRequest): Result<ServiceRequest>

    suspend fun getCustomerRequests(customerId: String?): Result<List<ServiceRequest>>

    suspend fun getProviderRequests(providerId: String): Result<List<ServiceRequest>>

    suspend fun acceptRequest(requestId: String, providerId: String): Result<ServiceRequest>

    suspend fun rejectRequest(requestId: String, providerId: String): Result<ServiceRequest>

    suspend fun completeRequest(requestId: String): Result<ServiceRequest>

    suspend fun cancelRequest(requestId: String): Result<ServiceRequest>
}
