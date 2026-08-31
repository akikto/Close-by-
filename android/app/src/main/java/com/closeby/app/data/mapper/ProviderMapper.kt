package com.closeby.app.data.mapper

import com.closeby.app.data.model.ProviderDto
import com.closeby.app.domain.model.Provider
import com.closeby.feature.servicelisting.domain.model.ServiceCategory

/**
 * Maps between the Supabase wire model (ProviderDto) and the domain
 * model (Provider). Keeps Postgres-shaped fields (snake_case, raw
 * strings) out of the domain layer.
 */
fun ProviderDto.toDomain(): Provider = Provider(
    id = id,
    name = name,
    category = parseCategory(category),
    phoneNumber = phoneNumber,
    latitude = latitude,
    longitude = longitude
)

private fun parseCategory(raw: String): ServiceCategory =
    runCatching { ServiceCategory.valueOf(raw.trim().uppercase()) }
        .getOrDefault(ServiceCategory.VEHICLES)
