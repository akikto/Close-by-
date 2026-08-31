package com.closeby.app.core.error

suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelayMs: Long = 500,
    block: suspend () -> Result<T>
): Result<T> {
    var delayMs = initialDelayMs
    var last: Result<T> = Result.failure(IllegalStateException("No attempts made."))
    repeat(times) { attempt ->
        last = block()
        if (last.isSuccess || attempt == times - 1) return last
        kotlinx.coroutines.delay(delayMs)
        delayMs *= 2
    }
    return last
}
