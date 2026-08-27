package io.horizontalsystems.marketkit

import io.horizontalsystems.marketkit.providers.ISchedulerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max

class Scheduler(
    private val provider: ISchedulerProvider,
    private val bufferInterval: Int = 5
) {
    private val retryInterval = 30L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timeJob: Job? = null
    private var syncJob: Job? = null

    private var isExpiredRatesNotified = false

    @Volatile
    private var stopped = false

    fun start(force: Boolean = false) {
        //  Force sync
        if (force) {
            onFire()
        } else {
            autoSchedule()
        }
    }

    @Synchronized
    fun stop() {
        stopped = true
        timeJob?.cancel()
        syncJob?.cancel()
        scope.cancel()
    }

    private fun autoSchedule(minDelay: Long = 0) {
        var newDelay = 0L

        provider.lastSyncTimestamp?.let { lastSync ->
            val diff = Date().time / 1000 - lastSync
            newDelay = max(0, provider.expirationInterval - bufferInterval - diff)
        }

        val delay = max(newDelay, minDelay)
        schedule(delay)
    }

    @Synchronized
    private fun schedule(delay: Long) {
        if (stopped) return

        notifyRatesIfExpired()

        timeJob?.cancel()
        timeJob = scope.launch {
            delay(delay * 1000)
            onFire()
        }
    }

    @Synchronized
    private fun onFire() {
        if (stopped) return

        syncJob?.cancel()
        syncJob = scope.launch {
            try {
                provider.sync()
                autoSchedule(retryInterval)
                isExpiredRatesNotified = false
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                schedule(retryInterval)
            }
        }
    }

    private fun notifyRatesIfExpired() {
        if (isExpiredRatesNotified) return

        val timestamp = provider.lastSyncTimestamp
        if (timestamp == null || Date().time / 1000 - timestamp > provider.expirationInterval) {
            provider.notifyExpired()
            isExpiredRatesNotified = true
        }
    }
}
