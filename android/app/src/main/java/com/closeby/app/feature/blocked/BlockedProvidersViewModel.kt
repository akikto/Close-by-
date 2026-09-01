package com.closeby.app.feature.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.app.data.remote.ProviderRemoteDataSource
import com.closeby.trust.domain.repository.TrustRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BlockedProviderItem(
    val providerId: String,
    val name: String
)

sealed interface BlockedProvidersUiState {
    data object Loading : BlockedProvidersUiState
    data object Empty : BlockedProvidersUiState
    data class Success(val providers: List<BlockedProviderItem>) : BlockedProvidersUiState
    data class Error(val message: String) : BlockedProvidersUiState
}

class BlockedProvidersViewModel(
    private val userId: String,
    private val trustRepository: TrustRepository,
    private val providerRemote: ProviderRemoteDataSource = ProviderRemoteDataSource()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockedProvidersUiState>(BlockedProvidersUiState.Loading)
    val uiState: StateFlow<BlockedProvidersUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = BlockedProvidersUiState.Loading
        viewModelScope.launch {
            trustRepository.getBlockedProviderIds(userId)
                .onSuccess { ids ->
                    if (ids.isEmpty()) {
                        _uiState.value = BlockedProvidersUiState.Empty
                        return@launch
                    }
                    val items = ids.mapNotNull { id ->
                        val dto = runCatching { providerRemote.getProviderById(id) }.getOrNull()
                        BlockedProviderItem(
                            providerId = id,
                            name = dto?.name ?: "Provider"
                        )
                    }
                    _uiState.value = if (items.isEmpty()) {
                        BlockedProvidersUiState.Empty
                    } else {
                        BlockedProvidersUiState.Success(items)
                    }
                }
                .onFailure { error ->
                    _uiState.value = BlockedProvidersUiState.Error(
                        error.message ?: "Could not load blocked providers."
                    )
                }
        }
    }

    fun unblock(providerId: String) {
        viewModelScope.launch {
            trustRepository.unblockProvider(userId, providerId)
                .onSuccess { load() }
                .onFailure { error ->
                    _uiState.value = BlockedProvidersUiState.Error(
                        error.message ?: "Could not unblock provider."
                    )
                }
        }
    }
}
