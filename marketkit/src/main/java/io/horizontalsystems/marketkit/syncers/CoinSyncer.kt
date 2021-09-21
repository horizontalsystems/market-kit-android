package io.horizontalsystems.marketkit.syncers

import android.util.Log
import io.horizontalsystems.marketkit.providers.HsProvider
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CoinSyncer(
    private val hsProvider: HsProvider
) {
    private var disposable: Disposable? = null

    fun sync() {
        disposable = hsProvider.getMarketCoins()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                Log.e("AAA", "${it.size} market coins fetched")
                it.forEach {
                    Log.e("AAA", it.toString())
                }
            }, {
                Log.e("AAA", "CoinSyncer error", it)
            })
    }

    fun stop() {
        disposable?.dispose()
        disposable = null
    }

}
