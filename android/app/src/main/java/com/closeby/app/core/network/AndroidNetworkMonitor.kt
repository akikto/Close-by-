package com.closeby.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidNetworkMonitor(context: Context) : NetworkMonitor {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _status = MutableStateFlow(readCurrentStatus())
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private var started = false
    private var wasOffline = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _status.value = if (wasOffline) NetworkStatus.RECONNECTING else NetworkStatus.ONLINE
            wasOffline = false
            // Promote reconnecting → online after brief availability
            if (_status.value == NetworkStatus.RECONNECTING) {
                _status.value = NetworkStatus.ONLINE
            }
        }

        override fun onLost(network: Network) {
            wasOffline = true
            _status.value = NetworkStatus.OFFLINE
        }

        override fun onUnavailable() {
            wasOffline = true
            _status.value = NetworkStatus.OFFLINE
        }
    }

    override fun start() {
        if (started) return
        started = true
        _status.value = readCurrentStatus()
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    override fun stop() {
        if (!started) return
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        started = false
    }

    private fun readCurrentStatus(): NetworkStatus {
        val network = connectivityManager.activeNetwork ?: return NetworkStatus.OFFLINE
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.OFFLINE
        return if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            NetworkStatus.ONLINE
        } else {
            NetworkStatus.OFFLINE
        }
    }
}
