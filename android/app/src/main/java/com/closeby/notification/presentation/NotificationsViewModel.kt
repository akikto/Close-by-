package com.closeby.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.closeby.notification.domain.model.AppNotification
import com.closeby.notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotificationsUiState {
    data object Loading : NotificationsUiState
    data object Empty : NotificationsUiState
    data class Loaded(val notifications: List<AppNotification>) : NotificationsUiState
    data class Error(val message: String) : NotificationsUiState
}

class NotificationsViewModel(
    private val userId: String?,
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun load() {
        val resolvedUserId = userId
        if (resolvedUserId.isNullOrBlank()) {
            _uiState.value = NotificationsUiState.Empty
            _unreadCount.value = 0
            NotificationUnreadHolder.update(0)
            return
        }

        _uiState.value = NotificationsUiState.Loading
        viewModelScope.launch {
            repository.getNotifications(resolvedUserId)
                .onSuccess { notifications ->
                    _uiState.value = if (notifications.isEmpty()) {
                        NotificationsUiState.Empty
                    } else {
                        NotificationsUiState.Loaded(notifications)
                    }
                    refreshUnreadCount(resolvedUserId)
                }
                .onFailure { error ->
                    _uiState.value = NotificationsUiState.Error(
                        error.message ?: "Failed to load notifications."
                    )
                }
        }
    }

    fun markRead(notification: AppNotification) {
        if (notification.isRead) return
        val resolvedUserId = userId ?: return
        viewModelScope.launch {
            repository.markRead(notification.id)
                .onSuccess {
                    refreshList(resolvedUserId)
                    refreshUnreadCount(resolvedUserId)
                }
        }
    }

    fun markAllRead() {
        val resolvedUserId = userId ?: return
        viewModelScope.launch {
            repository.markAllRead(resolvedUserId)
                .onSuccess {
                    refreshList(resolvedUserId)
                    refreshUnreadCount(resolvedUserId)
                }
        }
    }

    private suspend fun refreshList(userId: String) {
        repository.getNotifications(userId)
            .onSuccess { notifications ->
                _uiState.value = if (notifications.isEmpty()) {
                    NotificationsUiState.Empty
                } else {
                    NotificationsUiState.Loaded(notifications)
                }
            }
    }

    private suspend fun refreshUnreadCount(userId: String) {
        repository.getUnreadCount(userId)
            .onSuccess { count ->
                _unreadCount.value = count
                NotificationUnreadHolder.update(count)
            }
    }
}
