package io.horizontalsystems.marketkit

import android.content.Context
import io.horizontalsystems.marketkit.managers.CoinCategoryManager
import io.horizontalsystems.marketkit.managers.CoinManager
import io.horizontalsystems.marketkit.managers.CoinPriceManager
import io.horizontalsystems.marketkit.managers.CoinPriceSyncManager
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.CoinPriceSchedulerFactory
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinCategoryStorage
import io.horizontalsystems.marketkit.storage.CoinPriceStorage
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.MarketDatabase
import io.horizontalsystems.marketkit.syncers.CoinCategorySyncer
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import io.reactivex.Observable

class MarketKit(
    private val coinManager: CoinManager,
    private val coinCategoryManager: CoinCategoryManager,
    private val coinSyncer: CoinSyncer,
    private val coinCategorySyncer: CoinCategorySyncer,
    private val coinPriceManager: CoinPriceManager,
    private val coinPriceSyncManager: CoinPriceSyncManager
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
        coinSyncer.sync()
        coinCategorySyncer.sync()
    }

    // Coin Prices

    fun refreshCoinPrices(currencyCode: String) {
        coinPriceSyncManager.refresh(currencyCode)
    }

    fun coinPrice(coinUid: String, currencyCode: String): CoinPrice? {
        return coinPriceManager.coinPrice(coinUid, currencyCode)
    }

    fun coinPriceMap(coinUids: List<String>, currencyCode: String): Map<String, CoinPrice> {
        return coinPriceManager.coinPriceMap(coinUids, currencyCode)
    }

    fun coinPriceObservable(coinUid: String, currencyCode: String): Observable<CoinPrice> {
        return coinPriceSyncManager.coinPriceObservable(coinUid, currencyCode)
    }

    fun coinPriceMapObservable(coinUids: List<String>, currencyCode: String): Observable<Map<String, CoinPrice>> {
        return coinPriceSyncManager.coinPriceMapObservable(coinUids, currencyCode)
    }

    companion object {
        fun getInstance(context: Context): MarketKit {
            val marketDatabase = MarketDatabase.getInstance(context)
            val coinManager = CoinManager(CoinStorage(marketDatabase))
            val coinCategoryManager = CoinCategoryManager(CoinCategoryStorage(marketDatabase))
            val hsProvider = HsProvider()
            val coinSyncer = CoinSyncer(hsProvider, coinManager)
            val coinCategorySyncer = CoinCategorySyncer(hsProvider, coinCategoryManager)
            val coinPriceManager = CoinPriceManager(CoinPriceStorage(marketDatabase))
            val coinPriceSchedulerFactory = CoinPriceSchedulerFactory(coinPriceManager, hsProvider)
            val coinPriceSyncManager = CoinPriceSyncManager(coinPriceSchedulerFactory)
            coinPriceManager.listener = coinPriceSyncManager

            return MarketKit(
                coinManager,
                coinCategoryManager,
                coinSyncer,
                coinCategorySyncer,
                coinPriceManager,
                coinPriceSyncManager
            )
        }
    }

}
