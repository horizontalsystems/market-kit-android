package io.horizontalsystems.marketkit.syncers

import android.util.Log
import io.horizontalsystems.marketkit.managers.ExchangeManager
import io.horizontalsystems.marketkit.providers.CoinGeckoProvider
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ExchangeSyncer(
    private val exchangeManager: ExchangeManager,
    private val coinGeckoProvider: CoinGeckoProvider
) {
    private var disposable: Disposable? = null

    fun sync() {
        disposable = coinGeckoProvider.exchangesSingle(250, 0)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ exchanges ->
                exchangeManager.handleFetched(exchanges)
            }, {
                Log.e("ExchangeSyncer", "Fetch error", it)
            })
    }

    fun stop() {
        disposable?.dispose()
        disposable = null
    }
}
