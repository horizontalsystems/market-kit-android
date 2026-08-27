package io.horizontalsystems.marketkit.syncers

import android.util.Log
import io.horizontalsystems.marketkit.providers.HsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HsDataSyncer(
    private val coinSyncer: CoinSyncer,
    private val hsProvider: HsProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun sync() {
        job?.cancel()
        job = scope.launch {
            try {
                val status = hsProvider.statusSingle()
                coinSyncer.sync(status.coins, status.blockchains, status.tokens)
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("CoinSyncer", "sync() error", e)
            }
        }
    }

}
