package io.horizontalsystems.marketkit

import android.content.Context
import io.horizontalsystems.marketkit.managers.*
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.CoinPriceSchedulerFactory
import io.horizontalsystems.marketkit.providers.CryptoCompareProvider
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinCategoryStorage
import io.horizontalsystems.marketkit.storage.CoinPriceStorage
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.MarketDatabase
import io.horizontalsystems.marketkit.syncers.CoinCategorySyncer
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import io.reactivex.Observable
import io.reactivex.Single

class MarketKit(
    private val coinManager: CoinManager,
    private val coinCategoryManager: CoinCategoryManager,
    private val coinSyncer: CoinSyncer,
    private val coinCategorySyncer: CoinCategorySyncer,
    private val coinPriceManager: CoinPriceManager,
    private val coinPriceSyncManager: CoinPriceSyncManager,
    private val postManager: PostManager
) {
    // Coins

    val fullCoinsUpdatedObservable: Observable<Unit>
        get() = coinManager.fullCoinsUpdatedObservable


    fun fullCoins(filter: String, limit: Int = 20): List<FullCoin> {
        return coinManager.fullCoins(filter, limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        return coinManager.fullCoins(coinUids)
    }

    fun fullCoinsByCoinTypes(coinTypes: List<CoinType>): List<FullCoin> {
        return coinManager.fullCoinsByCoinTypes(coinTypes)
    }

    fun marketInfosSingle(
        top: Int = 250,
        limit: Int? = null,
        order: MarketInfo.Order? = null
    ): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(top, limit, order)
    }

    fun marketInfosSingle(coinUids: List<String>, order: MarketInfo.Order? = null): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(coinUids, order)
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

    // Posts

    fun postsSingle(): Single<List<Post>> {
        return postManager.postsSingle()
    }

    companion object {
        fun getInstance(context: Context, hsApiBaseUrl: String, cryptoCompareApiKey: String? = null): MarketKit {
            val marketDatabase = MarketDatabase.getInstance(context)
            val hsProvider = HsProvider(hsApiBaseUrl)
            val coinManager = CoinManager(CoinStorage(marketDatabase), hsProvider)
            val coinCategoryManager = CoinCategoryManager(CoinCategoryStorage(marketDatabase))
            val coinSyncer = CoinSyncer(hsProvider, coinManager)
            val coinCategorySyncer = CoinCategorySyncer(hsProvider, coinCategoryManager)
            val coinPriceManager = CoinPriceManager(CoinPriceStorage(marketDatabase))
            val coinPriceSchedulerFactory = CoinPriceSchedulerFactory(coinPriceManager, hsProvider)
            val coinPriceSyncManager = CoinPriceSyncManager(coinPriceSchedulerFactory)
            coinPriceManager.listener = coinPriceSyncManager
            val cryptoCompareProvider = CryptoCompareProvider(cryptoCompareApiKey)
            val postManager = PostManager(cryptoCompareProvider)

            return MarketKit(
                coinManager,
                coinCategoryManager,
                coinSyncer,
                coinCategorySyncer,
                coinPriceManager,
                coinPriceSyncManager,
                postManager
            )
        }
    }

}
