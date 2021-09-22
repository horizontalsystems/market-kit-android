package io.horizontalsystems.marketkit.demo

import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.syncers.CoinCategorySyncer
import io.horizontalsystems.marketkit.syncers.CoinSyncer

class MainViewModel(
    private val coinSyncer: CoinSyncer,
    private val coinCategorySyncer: CoinCategorySyncer
) : ViewModel() {

    fun syncCoins() {
        coinSyncer.sync()
        coinCategorySyncer.sync()
    }

}
