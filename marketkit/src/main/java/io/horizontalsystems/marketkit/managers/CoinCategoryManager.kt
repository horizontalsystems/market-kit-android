package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.CoinCategory
import io.horizontalsystems.marketkit.models.CoinCategoryMarketData
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinCategoryStorage
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class CoinCategoryManager(
    private val storage: CoinCategoryStorage,
    private val hsProvider: HsProvider
) {
    val coinCategoriesObservable = PublishSubject.create<List<CoinCategory>>()

    fun coinCategories(): List<CoinCategory> {
        return storage.coinCategories()
    }

    fun coinCategories(uids: List<String>): List<CoinCategory> {
        return storage.coinCategories(uids)
    }

    fun coinCategory(uid: String): CoinCategory? {
        return storage.coinCategory(uid)
    }

    fun handleFetched(coinCategories: List<CoinCategory>) {
        storage.save(coinCategories)
        coinCategoriesObservable.onNext(coinCategories)
    }

    fun coinCategoriesMarketDataSingle(currencyCode: String): Single<List<CoinCategoryMarketData>> {
        return hsProvider.coinCategoriesMarketDataSingle(currencyCode)
    }

    fun coinCategoryMarketPointsSingle(categoryUid: String, interval: HsTimePeriod) =
        hsProvider.coinCategoryMarketPointsSingle(categoryUid, interval)
}
