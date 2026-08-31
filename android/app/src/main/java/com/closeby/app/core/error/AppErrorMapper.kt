package com.closeby.app.core.error

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object AppErrorMapper {

    fun toUserMessage(throwable: Throwable?): String {
        if (throwable == null) return "Something went wrong. Please try again."
        return when (throwable) {
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException -> "Unable to connect. Check your internet connection and try again."
            is NoSuchElementException -> "The requested item could not be found."
            is SecurityException -> "You do not have permission to perform this action."
            is IllegalStateException -> throwable.message?.takeIf { it.isNotBlank() }
                ?: "This action is not available right now."
            is IllegalArgumentException -> throwable.message?.takeIf { it.isNotBlank() }
                ?: "Please check your input and try again."
            else -> throwable.message?.takeIf { it.isNotBlank() && !looksTechnical(it) }
                ?: "Something went wrong. Please try again."
        }
    }

    fun offlineMessage(): String =
        "You're offline. Connect to the internet to continue."

    fun offlineRequestMessage(): String =
        "You're offline. Connect to the internet to submit this request."

    private fun looksTechnical(message: String): Boolean =
        message.contains("Exception", ignoreCase = true) ||
            message.contains("kotlin.", ignoreCase = true) ||
            message.contains("java.", ignoreCase = true)
}
