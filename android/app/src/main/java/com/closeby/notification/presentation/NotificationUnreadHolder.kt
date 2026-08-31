package com.closeby.notification.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Shared unread badge count for the bottom navigation bar. */
object NotificationUnreadHolder {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun update(count: Int) {
        _unreadCount.value = count.coerceAtLeast(0)
    }
}
