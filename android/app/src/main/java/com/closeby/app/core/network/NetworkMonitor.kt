package com.closeby.app.core.network

import kotlinx.coroutines.flow.StateFlow

interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>
    fun start()
    fun stop()
    fun isOnline(): Boolean = status.value == NetworkStatus.ONLINE
}
