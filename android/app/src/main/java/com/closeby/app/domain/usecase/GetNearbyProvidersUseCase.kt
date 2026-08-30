package com.closeby.app.domain.usecase

import com.closeby.app.domain.model.Provider
import com.closeby.app.domain.repository.ProviderRepository

/**
 * Example use case showing the intended UI -> ViewModel -> UseCase ->
 * Repository -> DataSource flow. The real nearby-search algorithm is
 * a later feature task; this is a thin pass-through for now.
 */
class GetNearbyProvidersUseCase(
    private val providerRepository: ProviderRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): List<Provider> {
        return providerRepository.getNearbyProviders(latitude, longitude, radiusKm)
    }
}
