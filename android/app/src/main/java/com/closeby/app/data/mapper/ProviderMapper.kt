package com.closeby.app.data.mapper

import com.closeby.app.data.model.ProviderDto
import com.closeby.app.domain.model.Provider
import com.closeby.app.domain.model.ServiceCategory

/**
 * Maps between the Supabase wire model (ProviderDto) and the domain
 * model (Provider). Keeps Postgres-shaped fields (snake_case, raw
 * strings) out of the domain layer.
 */
fun ProviderDto.toDomain(): Provider = Provider(
    id = id,
    name = name,
    category = ServiceCategory.valueOf(category.uppercase()),
    phoneNumber = phone_number,
    latitude = latitude,
    longitude = longitude
)
