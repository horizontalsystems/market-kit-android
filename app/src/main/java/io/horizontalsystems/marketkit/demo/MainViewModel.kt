package io.horizontalsystems.marketkit.demo

import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.syncers.CoinSyncer

class MainViewModel : ViewModel() {

    private val hsProvider = HsProvider()
    private val coinSyncer = CoinSyncer(hsProvider)

    init {

    }

    fun syncCoins() {
        coinSyncer.sync()
    }

}
