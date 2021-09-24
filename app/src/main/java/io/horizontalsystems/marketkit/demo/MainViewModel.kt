package io.horizontalsystems.marketkit.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.MarketKit
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(
    private val marketKit: MarketKit
) : ViewModel() {

    private var disposable: Disposable? = null

    fun syncCoins() {
        marketKit.sync()
        marketKit.refreshCoinPrices("USD")

        disposable?.dispose()
        disposable = marketKit.coinPriceMapObservable(listOf("bitcoin", "ethereum", "solana"), "USD")
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e("AAA", "coinPrices: ${it.size}")
                it.forEach {
                    Log.e("AAA", "coinPrice ${it.key}: ${it.value}")
                }
            }, {
                Log.e("AAA", "coinPriceMapObservable error", it)
            })
    }

}
