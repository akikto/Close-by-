package com.closeby.request.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * A customer's request for a service, sent to a provider without any
 * online payment. Budget is negotiation information only — see
 * [BudgetUnit] and [budgetAmount].
 *
 * [customerId] is nullable because customer login is NOT mandatory in
 * Close by; anonymous requests must be supported by the architecture.
 */
data class ServiceRequest(
    val id: String,
    val serviceId: String,
    val providerId: String,
    val customerId: String?,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val serviceTitle: String,
    val requestedDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val duration: String,
    val budgetAmount: Double? = null,
    val budgetCurrency: String = "INR",
    val budgetUnit: BudgetUnit? = null,
    val note: String? = null,
    val clientSessionId: String? = null,
    val providerName: String? = null,
    val providerPhone: String? = null,
    val status: ServiceRequestStatus = ServiceRequestStatus.PENDING,
    val createdAt: Long,
    val updatedAt: Long
)
