package com.closeby.app.core.network

import android.content.Context

object NetworkMonitorHolder {
    @Volatile
    private var monitor: NetworkMonitor? = null

    fun get(context: Context): NetworkMonitor {
        return monitor ?: synchronized(this) {
            monitor ?: AndroidNetworkMonitor(context.applicationContext).also {
                monitor = it
                it.start()
            }
        }
    }
}
