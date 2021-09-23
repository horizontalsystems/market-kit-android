package io.horizontalsystems.marketkit

import android.content.Context
import io.horizontalsystems.marketkit.managers.CoinCategoryManager
import io.horizontalsystems.marketkit.managers.CoinManager
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinCategoryStorage
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.MarketDatabase
import io.horizontalsystems.marketkit.syncers.CoinCategorySyncer
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class MarketKit(
    private val coinManager: CoinManager,
    private val coinCategoryManager: CoinCategoryManager,
    private val coinSyncer: CoinSyncer,
    private val coinCategorySyncer: CoinCategorySyncer
) {

    // Coins

    val marketCoinsUpdatedObservable: Observable<Unit>
        get() = coinManager.marketCoinsUpdatedObservable


    fun marketCoins(filter: String, limit: Int = 20): List<MarketCoin> {
        return coinManager.marketCoins(filter, limit)
    }

    fun marketCoins(coinUids: List<String>): List<MarketCoin> {
        return coinManager.marketCoins(coinUids)
    }

    fun marketCoinsByCoinTypes(coinTypes: List<CoinType>): List<MarketCoin> {
        return coinManager.marketCoinsByCoinTypes(coinTypes)
    }

    fun platformCoin(coinType: CoinType): PlatformCoin? {
        return coinManager.platformCoin(coinType)
    }

    fun platformCoins(): List<PlatformCoin> {
        return coinManager.platformCoins()
    }

    fun platformCoins(coinTypes: List<CoinType>): List<PlatformCoin> {
        return coinManager.platformCoins(coinTypes)
    }

    fun platformCoinsByCoinTypeIds(coinTypeIds: List<String>): List<PlatformCoin> {
        return coinManager.platformCoinsByCoinTypeIds(coinTypeIds)
    }

    fun coins(filter: String, limit: Int = 20): List<Coin> {
        return coinManager.coins(filter, limit)
    }

    // Categories

    val coinCategoriesObservable: Observable<List<CoinCategory>>
        get() = coinCategoryManager.coinCategoriesObservable


    fun coinCategories(): List<CoinCategory> {
        return coinCategoryManager.coinCategories()
    }

    fun sync() {
//        coinSyncer.sync()
//        coinCategorySyncer.sync()
    }

    // Coin Prices

    fun refreshCoinPrices(currencyCode: String) {
        // TODO
    }

    fun coinPrice(coinUid: String, currencyCode: String): CoinPrice? {
        // TODO
        return null
    }

    fun coinPriceMap(coinUids: List<String>, currencyCode: String): Map<String, CoinPrice> {
        // TODO
        return mapOf()
    }

    private val coinPriceObservable = PublishSubject.create<CoinPrice>()
    fun coinPriceObservable(coinUid: String, currencyCode: String): Observable<CoinPrice> {
        // TODO
        return coinPriceObservable
    }

    private val coinPriceMapObservable = PublishSubject.create<Map<String, CoinPrice>>()
    fun coinPriceMapObservable(coinUids: List<String>, currencyCode: String): Observable<Map<String, CoinPrice>> {
        // TODO
        return coinPriceMapObservable
    }


    companion object {
        fun getInstance(context: Context): MarketKit {
            val coinStorage = CoinStorage(MarketDatabase.getInstance(context))
            val coinCategoryStorage = CoinCategoryStorage(MarketDatabase.getInstance(context))
            val coinManager = CoinManager(coinStorage)
            val coinCategoryManager = CoinCategoryManager(coinCategoryStorage)
            val coinSyncer = CoinSyncer(HsProvider(), coinManager)
            val coinCategorySyncer = CoinCategorySyncer(HsProvider(), coinCategoryManager)

            return MarketKit(coinManager, coinCategoryManager, coinSyncer, coinCategorySyncer)
        }
    }
}