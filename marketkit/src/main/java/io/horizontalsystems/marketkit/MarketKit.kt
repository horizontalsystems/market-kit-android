package io.horizontalsystems.marketkit

import android.content.Context
import android.os.storage.StorageManager
import io.horizontalsystems.marketkit.chart.ChartManager
import io.horizontalsystems.marketkit.chart.ChartSchedulerFactory
import io.horizontalsystems.marketkit.chart.ChartSyncManager
import io.horizontalsystems.marketkit.managers.*
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.*
import io.horizontalsystems.marketkit.storage.*
import io.horizontalsystems.marketkit.syncers.CoinCategorySyncer
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import io.horizontalsystems.marketkit.syncers.ExchangeSyncer
import io.reactivex.Observable
import io.reactivex.Single

class MarketKit(
    private val coinManager: CoinManager,
    private val coinCategoryManager: CoinCategoryManager,
    private val coinSyncer: CoinSyncer,
    private val coinCategorySyncer: CoinCategorySyncer,
    private val coinPriceManager: CoinPriceManager,
    private val coinPriceSyncManager: CoinPriceSyncManager,
    private val postManager: PostManager,
    private val chartManager: ChartManager,
    private val exchangeSyncer: ExchangeSyncer,
    private val chartSyncManager: ChartSyncManager,
    private val globalMarketInfoManager: GlobalMarketInfoManager
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

    fun marketInfosSingle(top: Int, currencyCode: String, defi: Boolean = false): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(top, currencyCode, defi)
    }

    fun advancedMarketInfosSingle(top: Int = 250, currencyCode: String): Single<List<MarketInfo>> {
        return coinManager.advancedMarketInfosSingle(top, currencyCode)
    }

    fun marketInfosSingle(coinUids: List<String>, currencyCode: String): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(coinUids, currencyCode)
    }

    fun marketInfosSingle(categoryUid: String, currencyCode: String): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(categoryUid, currencyCode)
    }

    fun marketInfoOverviewSingle(
        coinUid: String,
        currencyCode: String,
        language: String
    ): Single<MarketInfoOverview> {
        return coinManager.marketInfoOverviewSingle(coinUid, currencyCode, language)
    }

    fun marketInfoDetailsSingle(coinUid: String, currencyCode: String): Single<MarketInfoDetails> {
        return coinManager.marketInfoDetailsSingle(coinUid, currencyCode)
    }

    fun marketInfoTvlSingle(coinUid: String, currencyCode: String, timePeriod: TimePeriod): Single<List<ChartPoint>> {
        return coinManager.marketInfoTvlSingle(coinUid, currencyCode, timePeriod)
    }

    fun defiMarketInfosSingle(currencyCode: String): Single<List<DefiMarketInfo>> {
        return coinManager.defiMarketInfosSingle(currencyCode)
    }

    fun platformCoin(coinType: CoinType): PlatformCoin? {
        return coinManager.platformCoin(coinType)
    }

    fun platformCoins(platformType: PlatformType, filter: String, limit: Int = 20): List<PlatformCoin> {
        return coinManager.platformCoins(platformType, filter, limit)
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

    fun coinCategory(uid: String): CoinCategory? {
        return coinCategoryManager.coinCategory(uid)
    }

    fun sync() {
        coinSyncer.sync()
        coinCategorySyncer.sync()
        exchangeSyncer.sync()
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

    fun coinPriceMapObservable(
        coinUids: List<String>,
        currencyCode: String
    ): Observable<Map<String, CoinPrice>> {
        return coinPriceSyncManager.coinPriceMapObservable(coinUids, currencyCode)
    }

    // Posts

    fun postsSingle(): Single<List<Post>> {
        return postManager.postsSingle()
    }

    // Market Tickers

    fun marketTickersSingle(coinUid: String): Single<List<MarketTicker>> {
        return coinManager.marketTickersSingle(coinUid)
    }

    // Details

    fun topHoldersSingle(coinUid: String): Single<List<TokenHolder>> {
        return coinManager.topHoldersSingle(coinUid)
    }

    fun treasuriesSingle(coinUid: String, currencyCode: String): Single<List<CoinTreasury>> {
        return coinManager.treasuriesSingle(coinUid, currencyCode)
    }

    fun investmentsSingle(coinUid: String, currencyCode: String): Single<List<CoinInvestment>> {
        return coinManager.investmentsSingle(coinUid, currencyCode)
    }

    fun coinReportsSingle(coinUid: String): Single<List<CoinReport>> {
        return coinManager.coinReportsSingle(coinUid)
    }

    fun auditReportsSingle(addresses: List<String>): Single<List<Auditor>> {
        return coinManager.auditReportsSingle(addresses)
    }

    // Chart Info

    fun chartInfo(coinUid: String, currencyCode: String, chartType: ChartType): ChartInfo? {
        return chartManager.getChartInfo(coinUid, currencyCode, chartType)
    }

    fun chartInfoSingle(coinUid: String, currencyCode: String, chartType: ChartType): Single<ChartInfo> {
        return chartManager.chartInfoSingle(coinUid, currencyCode, chartType)
    }

    fun getChartInfoAsync(
        coinUid: String,
        currencyCode: String,
        chartType: ChartType
    ): Observable<ChartInfo> {
        return chartSyncManager.chartInfoObservable(coinUid, currencyCode, chartType)
    }

    // Global Market Info

    fun globalMarketPointsSingle(currencyCode: String, timePeriod: TimePeriod): Single<List<GlobalMarketPoint>> {
        return globalMarketInfoManager.globalMarketInfoSingle(currencyCode, timePeriod)
    }

    companion object {
        fun getInstance(
            context: Context,
            hsApiBaseUrl: String,
            hsOldApiBaseUrl: String,
            cryptoCompareApiKey: String? = null,
            defiYieldApiKey: String? = null
        ): MarketKit {
            // init cache
            (context.getSystemService(Context.STORAGE_SERVICE) as StorageManager?)?.let { storageManager ->
                val cacheDir = context.cacheDir
                val cacheQuotaBytes = storageManager.getCacheQuotaBytes(storageManager.getUuidForPath(cacheDir))

                HSCache.cacheDir = cacheDir
                HSCache.cacheQuotaBytes = cacheQuotaBytes
            }

            val marketDatabase = MarketDatabase.getInstance(context)
            val hsProvider = HsProvider(hsApiBaseUrl, hsOldApiBaseUrl)
            val coinCategoryManager = CoinCategoryManager(CoinCategoryStorage(marketDatabase))
            val coinGeckoProvider = CoinGeckoProvider("https://api.coingecko.com/api/v3/")
            val defiYieldProvider = DefiYieldProvider(defiYieldApiKey)
            val exchangeManager = ExchangeManager(ExchangeStorage(marketDatabase))
            val exchangeSyncer = ExchangeSyncer(exchangeManager, coinGeckoProvider)
            val coinManager =
                CoinManager(
                    CoinStorage(marketDatabase),
                    hsProvider,
                    coinCategoryManager,
                    coinGeckoProvider,
                    defiYieldProvider,
                    exchangeManager
                )
            val coinSyncer = CoinSyncer(hsProvider, coinManager)
            val coinCategorySyncer = CoinCategorySyncer(hsProvider, coinCategoryManager)
            val coinPriceManager = CoinPriceManager(CoinPriceStorage(marketDatabase))
            val coinPriceSchedulerFactory = CoinPriceSchedulerFactory(coinPriceManager, hsProvider)
            val coinPriceSyncManager = CoinPriceSyncManager(coinPriceSchedulerFactory)
            coinPriceManager.listener = coinPriceSyncManager
            val cryptoCompareProvider = CryptoCompareProvider(cryptoCompareApiKey)
            val postManager = PostManager(cryptoCompareProvider)
            val chartManager = ChartManager(coinManager, ChartPointStorage(marketDatabase), coinGeckoProvider)
            val chartSchedulerFactory = ChartSchedulerFactory(chartManager, coinGeckoProvider)
            val chartSyncManager = ChartSyncManager(coinManager, chartSchedulerFactory).also {
                chartManager.listener = it
            }
            val globalMarketInfoStorage = GlobalMarketInfoStorage(marketDatabase)
            val globalMarketInfoManager = GlobalMarketInfoManager(hsProvider, globalMarketInfoStorage)

            return MarketKit(
                coinManager,
                coinCategoryManager,
                coinSyncer,
                coinCategorySyncer,
                coinPriceManager,
                coinPriceSyncManager,
                postManager,
                chartManager,
                exchangeSyncer,
                chartSyncManager,
                globalMarketInfoManager,
            )
        }
    }

}

//Errors

class NoChartInfo : Exception()

sealed class ProviderError : Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class NoCoinGeckoId : ProviderError()
}