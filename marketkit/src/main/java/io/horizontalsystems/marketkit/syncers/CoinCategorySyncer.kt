package io.horizontalsystems.marketkit.syncers

import android.util.Log
import io.horizontalsystems.marketkit.managers.CoinCategoryManager
import io.horizontalsystems.marketkit.providers.HsProvider
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CoinCategorySyncer(
    private val hsProvider: HsProvider,
    private val coinCategoryManager: CoinCategoryManager
) {
    private var disposable: Disposable? = null

    fun sync() {
        disposable = hsProvider.getCoinCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ categories ->
                Log.e("AAA", "${categories.size} categories fetched")
                categories.forEach {
                    Log.e("AAA", it.toString())
                }
                coinCategoryManager.handleFetched(categories)
            }, {
                Log.e("AAA", "CoinCategorySyncer error", it)
            })
    }

    fun stop() {
        disposable?.dispose()
        disposable = null
    }
}
