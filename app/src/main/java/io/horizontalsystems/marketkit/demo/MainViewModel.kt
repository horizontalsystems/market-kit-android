package io.horizontalsystems.marketkit.demo

import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.syncers.CoinSyncer

class MainViewModel(
    private val coinSyncer: CoinSyncer
) : ViewModel() {

    fun syncCoins() {
        coinSyncer.sync()
    }

}
