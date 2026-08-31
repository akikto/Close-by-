package com.closeby.request.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceRequestDto(
    val id: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_phone") val customerPhone: String? = null,
    @SerialName("service_title") val serviceTitle: String,
    @SerialName("requested_date") val requestedDate: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val duration: String = "",
    @SerialName("budget_amount") val budgetAmount: Double? = null,
    @SerialName("budget_currency") val budgetCurrency: String = "INR",
    @SerialName("budget_unit") val budgetUnit: String? = null,
    val note: String? = null,
    val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ServiceRequestInsertDto(
    @SerialName("service_id") val serviceId: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_phone") val customerPhone: String? = null,
    @SerialName("service_title") val serviceTitle: String,
    @SerialName("requested_date") val requestedDate: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val duration: String,
    @SerialName("budget_amount") val budgetAmount: Double? = null,
    @SerialName("budget_currency") val budgetCurrency: String = "INR",
    @SerialName("budget_unit") val budgetUnit: String? = null,
    val note: String? = null,
    val status: String = "PENDING"
)

@Serializable
data class ServiceRequestStatusUpdateDto(
    val status: String,
    @SerialName("updated_at") val updatedAt: String
)
