package io.horizontalsystems.marketkit.providers

import kotlinx.coroutines.delay
import kotlin.reflect.KClass

suspend fun <T> retryWhenError(
    errorForRetry: KClass<*>,
    maxRetries: Int = 3,
    block: suspend () -> T
): T {
    var retryCounter = 0L
    while (true) {
        try {
            return block()
        } catch (error: Throwable) {
            if (errorForRetry.isInstance(error) && retryCounter++ < maxRetries) {
                delay(retryCounter * 1000)
            } else {
                throw error
            }
        }
    }
}
