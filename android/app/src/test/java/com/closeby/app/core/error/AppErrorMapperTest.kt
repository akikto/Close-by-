package com.closeby.app.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException

class AppErrorMapperTest {

    @Test
    fun mapsNetworkErrorsToFriendlyMessage() {
        val message = AppErrorMapper.toUserMessage(UnknownHostException())
        assertEquals(
            "Unable to connect. Check your internet connection and try again.",
            message
        )
    }

    @Test
    fun mapsIOExceptionToFriendlyMessage() {
        val message = AppErrorMapper.toUserMessage(IOException("broken pipe"))
        assertFalse(message.contains("IOException"))
    }

    @Test
    fun offlineRequestMessageIsActionable() {
        assertEquals(
            "You're offline. Connect to the internet to submit this request.",
            AppErrorMapper.offlineRequestMessage()
        )
    }
}
