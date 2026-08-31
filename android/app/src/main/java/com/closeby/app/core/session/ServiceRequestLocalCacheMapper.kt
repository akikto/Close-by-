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
) {
    fun toDomain(): ServiceRequest = ServiceRequest(
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
        status = runCatching { ServiceRequestStatus.valueOf(status) }
            .getOrDefault(ServiceRequestStatus.PENDING),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun from(request: ServiceRequest) = CachedServiceRequest(
            id = request.id,
            serviceId = request.serviceId,
            providerId = request.providerId,
            customerId = request.customerId,
            customerName = request.customerName,
            customerPhone = request.customerPhone,
            serviceTitle = request.serviceTitle,
            requestedDate = request.requestedDate.toString(),
            startTime = request.startTime.toString(),
            endTime = request.endTime.toString(),
            duration = request.duration,
            budgetAmount = request.budgetAmount,
            budgetCurrency = request.budgetCurrency,
            budgetUnit = request.budgetUnit?.name,
            note = request.note,
            clientSessionId = request.clientSessionId,
            providerName = request.providerName,
            providerPhone = request.providerPhone,
            status = request.status.name,
            createdAt = request.createdAt,
            updatedAt = request.updatedAt
        )
    }
}

internal object ServiceRequestLocalCacheMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(request: ServiceRequest): String =
        json.encodeToString(CachedServiceRequest.from(request))

    fun decode(raw: String): ServiceRequest? = runCatching {
        json.decodeFromString<CachedServiceRequest>(raw).toDomain()
    }.getOrNull()
}
