package com.closeby.app.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkStatusTest {

    @Test
    fun statusesAreDistinct() {
        val values = NetworkStatus.entries.toSet()
        assertEquals(3, values.size)
        assertEquals(setOf(NetworkStatus.ONLINE, NetworkStatus.OFFLINE, NetworkStatus.RECONNECTING), values)
    }
}
