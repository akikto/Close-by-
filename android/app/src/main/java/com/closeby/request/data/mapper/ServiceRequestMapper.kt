package com.closeby.request.data.mapper

import com.closeby.request.data.model.ServiceRequestDto
import com.closeby.request.data.model.ServiceRequestInsertDto
import com.closeby.request.domain.model.BudgetUnit
import com.closeby.request.domain.model.ServiceRequest
import com.closeby.request.domain.model.ServiceRequestStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ServiceRequestMapper {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun toDomain(dto: ServiceRequestDto): ServiceRequest? {
        val status = parseStatus(dto.status) ?: return null
        val budgetUnit = dto.budgetUnit?.let { parseBudgetUnit(it) }
        return ServiceRequest(
            id = dto.id,
            serviceId = dto.serviceId,
            providerId = dto.providerId,
            customerId = dto.customerId,
            customerName = dto.customerName,
            customerPhone = dto.customerPhone,
            serviceTitle = dto.serviceTitle,
            requestedDate = runCatching { LocalDate.parse(dto.requestedDate, dateFormatter) }.getOrNull()
                ?: return null,
            startTime = runCatching { LocalTime.parse(dto.startTime, timeFormatter) }.getOrNull()
                ?: return null,
            endTime = runCatching { LocalTime.parse(dto.endTime, timeFormatter) }.getOrNull()
                ?: return null,
            duration = dto.duration,
            budgetAmount = dto.budgetAmount,
            budgetCurrency = dto.budgetCurrency,
            budgetUnit = budgetUnit,
            note = dto.note,
            status = status,
            clientSessionId = dto.clientSessionId,
            providerName = dto.providerName,
            providerPhone = dto.providerPhone,
            createdAt = Instant.parse(dto.createdAt).toEpochMilli(),
            updatedAt = Instant.parse(dto.updatedAt).toEpochMilli()
        )
    }

    fun toInsertDto(request: ServiceRequest): ServiceRequestInsertDto =
        ServiceRequestInsertDto(
            serviceId = request.serviceId,
            providerId = request.providerId,
            customerId = request.customerId,
            customerName = request.customerName,
            customerPhone = request.customerPhone,
            serviceTitle = request.serviceTitle,
            requestedDate = request.requestedDate.format(dateFormatter),
            startTime = request.startTime.format(timeFormatter),
            endTime = request.endTime.format(timeFormatter),
            duration = request.duration,
            budgetAmount = request.budgetAmount,
            budgetCurrency = request.budgetCurrency,
            budgetUnit = request.budgetUnit?.name,
            note = request.note,
            status = request.status.name,
            clientSessionId = request.clientSessionId,
            providerName = request.providerName,
            providerPhone = request.providerPhone
        )

    private fun parseStatus(raw: String): ServiceRequestStatus? =
        runCatching { ServiceRequestStatus.valueOf(raw.trim().uppercase()) }.getOrNull()

    private fun parseBudgetUnit(raw: String): BudgetUnit? =
        runCatching { BudgetUnit.valueOf(raw.trim().uppercase()) }.getOrNull()
}
