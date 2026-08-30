package com.closeby.util

/**
 * Generic immutable UI state used across Agent 5's ViewModels
 * (contact, request, availability).
 *
 * ViewModels expose this as a read-only StateFlow<UiState<T>> and never
 * expose a mutable flow publicly.
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
