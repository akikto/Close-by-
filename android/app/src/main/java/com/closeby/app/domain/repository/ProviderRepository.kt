package com.closeby.app.domain.repository

import com.closeby.app.domain.model.Provider

/**
 * Contract for reading provider data. Backed by Supabase/PostgreSQL
 * in the data layer.
 */
interface ProviderRepository {
    suspend fun getProviderById(id: String): Provider?
    suspend fun getNearbyProviders(latitude: Double, longitude: Double, radiusKm: Double): List<Provider>
}
