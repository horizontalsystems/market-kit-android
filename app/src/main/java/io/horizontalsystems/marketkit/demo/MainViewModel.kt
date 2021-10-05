package io.horizontalsystems.marketkit.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.models.MarketInfo
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(private val marketKit: MarketKit) : ViewModel() {
    private val disposables = CompositeDisposable()

    fun run() {
        syncCoins()
        fetchMarketInfos(listOf("bitcoin", "ethereum", "solana", "ripple"))
    }

    private fun syncCoins() {
        marketKit.sync()
        marketKit.refreshCoinPrices("USD")

        marketKit.coinPriceMapObservable(listOf("bitcoin", "ethereum", "solana"), "USD")
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e("AAA", "coinPrices: ${it.size}")
                it.forEach {
                    Log.e("AAA", "coinPrice ${it.key}: ${it.value}")
                }
            }, {
                Log.e("AAA", "coinPriceMapObservable error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun fetchMarketInfos(
        top: Int = 250,
        limit: Int? = null,
        order: MarketInfo.Order? = null,
    ) {
        marketKit.marketInfosSingle(top, limit, order)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.e("AAA", "marketInfo: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun fetchMarketInfos(
        coinUids: List<String>,
        order: MarketInfo.Order? = null,
    ) {
        marketKit.marketInfosSingle(coinUids, order)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.e("AAA", "marketInfo: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    override fun onCleared() {
        disposables.clear()
    }
}
