package com.closeby.app.core.session

import com.closeby.request.domain.model.BudgetUnit
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime

@Serializable
internal data class CachedServiceRequest(
    val id: String,
    val serviceId: String,
    val providerId: String,
    val customerId: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val serviceTitle: String,
    val requestedDate: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    val budgetAmount: Double? = null,
    val budgetCurrency: String = "INR",
    val budgetUnit: String? = null,
    val note: String? = null,
    val clientSessionId: String? = null,
    val providerName: String? = null,
    val providerPhone: String? = null,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

internal object ServiceRequestLocalCacheMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(request: ServiceRequest): String = json.encodeToString(request.toCached())

    fun decode(raw: String): ServiceRequest? = runCatching {
        json.decodeFromString<CachedServiceRequest>(raw).toDomain()
    }.getOrNull()

    private fun ServiceRequest.toCached() = CachedServiceRequest(
        id = id,
        serviceId = serviceId,
        providerId = providerId,
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        serviceTitle = serviceTitle,
        requestedDate = requestedDate.toString(),
        startTime = startTime.toString(),
        endTime = endTime.toString(),
        duration = duration,
        budgetAmount = budgetAmount,
        budgetCurrency = budgetCurrency,
        budgetUnit = budgetUnit?.name,
        note = note,
        clientSessionId = clientSessionId,
        providerName = providerName,
        providerPhone = providerPhone,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun CachedServiceRequest.toDomain(): ServiceRequest = ServiceRequest(
        id = id,
        serviceId = serviceId,
        providerId = providerId,
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        serviceTitle = serviceTitle,
        requestedDate = LocalDate.parse(requestedDate),
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        duration = duration,
        budgetAmount = budgetAmount,
        budgetCurrency = budgetCurrency,
        budgetUnit = budgetUnit?.let { runCatching { BudgetUnit.valueOf(it) }.getOrNull() },
        note = note,
        clientSessionId = clientSessionId,
        providerName = providerName,
        providerPhone = providerPhone,
        status = runCatching { ServiceRequestStatus.valueOf(status) }.getOrDefault(ServiceRequestStatus.PENDING),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
