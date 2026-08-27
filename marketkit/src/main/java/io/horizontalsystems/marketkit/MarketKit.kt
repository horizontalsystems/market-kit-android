package io.horizontalsystems.marketkit

import android.content.Context
import android.os.storage.StorageManager
import io.horizontalsystems.marketkit.chart.HsChartRequestHelper
import io.horizontalsystems.marketkit.managers.CoinHistoricalPriceManager
import io.horizontalsystems.marketkit.managers.CoinManager
import io.horizontalsystems.marketkit.managers.CoinPriceManager
import io.horizontalsystems.marketkit.managers.CoinPriceSyncManager
import io.horizontalsystems.marketkit.managers.DumpManager
import io.horizontalsystems.marketkit.managers.GlobalMarketInfoManager
import io.horizontalsystems.marketkit.managers.MarketOverviewManager
import io.horizontalsystems.marketkit.managers.NftManager
import io.horizontalsystems.marketkit.managers.PostManager
import io.horizontalsystems.marketkit.models.Analytics
import io.horizontalsystems.marketkit.models.AnalyticsPreview
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Category
import io.horizontalsystems.marketkit.models.ChartPoint
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.CoinCategory
import io.horizontalsystems.marketkit.models.CoinInvestment
import io.horizontalsystems.marketkit.models.CoinPrice
import io.horizontalsystems.marketkit.models.CoinReport
import io.horizontalsystems.marketkit.models.CoinTreasury
import io.horizontalsystems.marketkit.models.DefiMarketInfo
import io.horizontalsystems.marketkit.models.Etf
import io.horizontalsystems.marketkit.models.EtfPoint
import io.horizontalsystems.marketkit.models.EtfPointResponse
import io.horizontalsystems.marketkit.models.EtfResponse
import io.horizontalsystems.marketkit.models.FullCoin
import io.horizontalsystems.marketkit.models.GlobalMarketPoint
import io.horizontalsystems.marketkit.models.HsPeriodType
import io.horizontalsystems.marketkit.models.HsPointTimePeriod
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.horizontalsystems.marketkit.models.MarketGlobal
import io.horizontalsystems.marketkit.models.MarketInfo
import io.horizontalsystems.marketkit.models.MarketInfoOverview
import io.horizontalsystems.marketkit.models.MarketOverview
import io.horizontalsystems.marketkit.models.MarketTicker
import io.horizontalsystems.marketkit.models.NftTopCollection
import io.horizontalsystems.marketkit.models.Post
import io.horizontalsystems.marketkit.models.RankMultiValue
import io.horizontalsystems.marketkit.models.RankValue
import io.horizontalsystems.marketkit.models.Stock
import io.horizontalsystems.marketkit.models.SubscriptionResponse
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenHolders
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TopMovers
import io.horizontalsystems.marketkit.models.TopPair
import io.horizontalsystems.marketkit.models.TopPlatform
import io.horizontalsystems.marketkit.models.TopPlatformMarketCapPoint
import io.horizontalsystems.marketkit.models.Vault
import io.horizontalsystems.marketkit.providers.CoinPriceSchedulerFactory
import io.horizontalsystems.marketkit.providers.CryptoCompareProvider
import io.horizontalsystems.marketkit.providers.HsNftProvider
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinHistoricalPriceStorage
import io.horizontalsystems.marketkit.storage.CoinPriceStorage
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.GlobalMarketInfoStorage
import io.horizontalsystems.marketkit.storage.MarketDatabase
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import io.horizontalsystems.marketkit.syncers.HsDataSyncer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import retrofit2.Response
import java.math.BigDecimal
import java.util.Date

class MarketKit(
    private val nftManager: NftManager,
    private val marketOverviewManager: MarketOverviewManager,
    private val coinManager: CoinManager,
    private val coinSyncer: CoinSyncer,
    private val coinPriceManager: CoinPriceManager,
    private val coinHistoricalPriceManager: CoinHistoricalPriceManager,
    private val coinPriceSyncManager: CoinPriceSyncManager,
    private val postManager: PostManager,
    private val globalMarketInfoManager: GlobalMarketInfoManager,
    private val hsProvider: HsProvider,
    private val hsDataSyncer: HsDataSyncer,
    private val dumpManager: DumpManager,
) {
    private val coinsMap by lazy { coinManager.allCoins().associateBy { it.uid } }

    // Coins

    val fullCoinsUpdatedObservable: SharedFlow<Unit>
        get() = coinSyncer.fullCoinsUpdatedObservable

    fun topFullCoins(limit: Int = 20): List<FullCoin> {
        return coinManager.topFullCoins(limit)
    }

    fun fullCoins(filter: String, limit: Int = 20): List<FullCoin> {
        return coinManager.fullCoins(filter, limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        return coinManager.fullCoins(coinUids)
    }

    fun fullCoinsByCoinCodes(coinCodes: List<String>): List<FullCoin> {
        return coinManager.fullCoinsByCoinCodes(coinCodes)
    }

    fun allCoins(): List<Coin> = coinManager.allCoins()

    fun token(query: TokenQuery): Token? =
        coinManager.token(query)

    fun tokens(queries: List<TokenQuery>): List<Token> =
        coinManager.tokens(queries)

    fun tokens(reference: String): List<Token> =
        coinManager.tokens(reference)

    fun tokens(blockchainType: BlockchainType, filter: String, limit: Int = 20): List<Token> =
        coinManager.tokens(blockchainType, filter, limit)

    fun blockchains(uids: List<String>): List<Blockchain> =
        coinManager.blockchains(uids)

    fun allBlockchains(): List<Blockchain> =
        coinManager.allBlockchains()

    fun blockchain(uid: String): Blockchain? =
        coinManager.blockchain(uid)

    suspend fun marketInfosSingle(
        top: Int,
        currencyCode: String,
        defi: Boolean,
    ): List<MarketInfo> {
        return coinManager.getMarketInfos(hsProvider.marketInfosSingle(top, currencyCode, defi))
    }

    suspend fun topCoinsMarketInfosSingle(top: Int, currencyCode: String): List<MarketInfo> {
        return coinManager.getMarketInfos(hsProvider.topCoinsMarketInfosSingle(top, currencyCode))
    }

    suspend fun advancedMarketInfosSingle(
        top: Int = 250,
        currencyCode: String,
    ): List<MarketInfo> {
        return coinManager.getMarketInfos(hsProvider.advancedMarketInfosSingle(top, currencyCode))
    }

    suspend fun marketInfosSingle(
        coinUids: List<String>,
        currencyCode: String,
    ): List<MarketInfo> {
        return coinManager.getMarketInfos(hsProvider.marketInfosSingle(coinUids, currencyCode))
    }

    suspend fun categoriesSingle(): List<Category> {
        return hsProvider.categoriesSingle()
    }

    suspend fun marketInfosSingle(
        categoryUid: String,
        currencyCode: String,
    ): List<MarketInfo> {
        return coinManager.getMarketInfos(hsProvider.marketInfosSingle(categoryUid, currencyCode))
    }

    suspend fun marketInfoOverviewSingle(
        coinUid: String,
        currencyCode: String,
        language: String,
        roiUids: List<String>,
        roiPeriods: List<HsTimePeriod>,
    ): MarketInfoOverview {
        val rawOverview = hsProvider.getMarketInfoOverview(
            coinUid = coinUid,
            currencyCode = currencyCode,
            language = language,
            roiUids = roiUids,
            roiPeriods = roiPeriods,
        )
        val fullCoin = coinManager.fullCoin(coinUid) ?: throw Exception("No Full Coin")

        return rawOverview.marketInfoOverview(fullCoin)
    }

    suspend fun marketInfoTvlSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<ChartPoint> {
        return hsProvider.marketInfoTvlSingle(coinUid, currencyCode, timePeriod)
    }

    suspend fun marketInfoGlobalTvlSingle(
        chain: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<ChartPoint> {
        return hsProvider.marketInfoGlobalTvlSingle(chain, currencyCode, timePeriod)
    }

    suspend fun defiMarketInfosSingle(currencyCode: String): List<DefiMarketInfo> {
        return coinManager.getDefiMarketInfos(hsProvider.defiMarketInfosSingle(currencyCode))
    }

    //Signals

    suspend fun coinsSignalsSingle(coinsUids: List<String>): Map<String, Analytics.TechnicalAdvice.Advice> {
        return hsProvider.coinsSignalsSingle(coinsUids).mapNotNull { coinSignal ->
            if (coinSignal.signal == null) null
            else coinSignal.uid to coinSignal.signal
        }.toMap()
    }


    // Categories

    suspend fun coinCategoriesSingle(currencyCode: String): List<CoinCategory> =
        hsProvider.getCoinCategories(currencyCode)

    suspend fun coinCategoryMarketPointsSingle(
        categoryUid: String,
        interval: HsTimePeriod,
        currencyCode: String
    ) =
        hsProvider.coinCategoryMarketPointsSingle(categoryUid, interval, currencyCode)

    fun sync() {
        hsDataSyncer.sync()
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

    fun coinPriceObservable(
        tag: String,
        coinUid: String,
        currencyCode: String
    ): Flow<CoinPrice> {
        return coinPriceSyncManager.coinPriceObservable(tag, coinUid, currencyCode)
    }

    fun coinPriceMapObservable(
        tag: String,
        coinUids: List<String>,
        currencyCode: String
    ): Flow<Map<String, CoinPrice>> {
        return coinPriceSyncManager.coinPriceMapObservable(tag, coinUids, currencyCode)
    }

    // Coin Historical Price

    suspend fun coinHistoricalPriceSingle(
        coinUid: String,
        currencyCode: String,
        timestamp: Long
    ): BigDecimal {
        return coinHistoricalPriceManager.coinHistoricalPriceSingle(
            coinUid,
            currencyCode,
            timestamp
        )
    }

    fun coinHistoricalPrice(coinUid: String, currencyCode: String, timestamp: Long): BigDecimal? {
        return coinHistoricalPriceManager.coinHistoricalPrice(coinUid, currencyCode, timestamp)
    }

    // Posts

    suspend fun postsSingle(): List<Post> {
        return postManager.postsSingle()
    }

    // Market Tickers

    suspend fun marketTickersSingle(coinUid: String, currencyCode: String): List<MarketTicker> {
        return hsProvider.marketTickers(coinUid, currencyCode)
    }

    // Details

    suspend fun tokenHoldersSingle(
        authToken: String,
        coinUid: String,
        blockchainUid: String
    ): TokenHolders {
        return hsProvider.tokenHoldersSingle(authToken, coinUid, blockchainUid)
    }

    suspend fun treasuriesSingle(coinUid: String, currencyCode: String): List<CoinTreasury> {
        return hsProvider.coinTreasuriesSingle(coinUid, currencyCode)
    }

    suspend fun investmentsSingle(coinUid: String): List<CoinInvestment> {
        return hsProvider.investmentsSingle(coinUid)
    }

    suspend fun coinReportsSingle(coinUid: String): List<CoinReport> {
        return hsProvider.coinReportsSingle(coinUid)
    }

    // Pro Data

    suspend fun cexVolumesSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<ChartPoint> {
        val periodType = HsPeriodType.ByPeriod(timePeriod)
        val currentTime = Date().time / 1000
        val fromTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
        val interval = HsPointTimePeriod.Day1
        return hsProvider.coinPriceChartSingle(coinUid, currencyCode, interval, fromTimestamp)
            .mapNotNull { chartCoinPrice ->
                chartCoinPrice.totalVolume?.let { volume ->
                    ChartPoint(volume, chartCoinPrice.timestamp, null)
                }
            }
    }

    suspend fun dexLiquiditySingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<Analytics.VolumePoint> {
        return hsProvider.dexLiquiditySingle(authToken, coinUid, currencyCode, timePeriod)
    }

    suspend fun dexVolumesSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<Analytics.VolumePoint> {
        return hsProvider.dexVolumesSingle(authToken, coinUid, currencyCode, timePeriod)
    }

    suspend fun transactionDataSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod,
        platform: String?
    ): List<Analytics.CountVolumePoint> {
        return hsProvider.transactionDataSingle(authToken, coinUid, timePeriod, platform)
    }

    suspend fun activeAddressesSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod
    ): List<Analytics.CountPoint> {
        return hsProvider.activeAddressesSingle(authToken, coinUid, timePeriod)
    }

    suspend fun analyticsPreviewSingle(
        coinUid: String,
        addresses: List<String>,
    ): AnalyticsPreview {
        return hsProvider.analyticsPreviewSingle(coinUid, addresses)
    }

    suspend fun analyticsSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
    ): Analytics {
        return hsProvider.analyticsSingle(authToken, coinUid, currencyCode)
    }

    suspend fun cexVolumeRanksSingle(
        authToken: String,
        currencyCode: String
    ): List<RankMultiValue> {
        return hsProvider.rankMultiValueSingle(authToken, "cex_volume", currencyCode)
    }

    suspend fun dexVolumeRanksSingle(
        authToken: String,
        currencyCode: String
    ): List<RankMultiValue> {
        return hsProvider.rankMultiValueSingle(authToken, "dex_volume", currencyCode)
    }

    suspend fun dexLiquidityRanksSingle(authToken: String, currencyCode: String): List<RankValue> {
        return hsProvider.rankValueSingle(authToken, "dex_liquidity", currencyCode)
    }

    suspend fun activeAddressRanksSingle(
        authToken: String,
        currencyCode: String
    ): List<RankMultiValue> {
        return hsProvider.rankMultiValueSingle(authToken, "address", currencyCode)
    }

    suspend fun transactionCountsRanksSingle(
        authToken: String,
        currencyCode: String
    ): List<RankMultiValue> {
        return hsProvider.rankMultiValueSingle(authToken, "tx_count", currencyCode)
    }

    suspend fun holderRanksSingle(authToken: String, currencyCode: String): List<RankValue> {
        return hsProvider.rankValueSingle(authToken, "holders", currencyCode)
    }

    suspend fun revenueRanksSingle(authToken: String, currencyCode: String): List<RankMultiValue> {
        return hsProvider.rankMultiValueSingle(authToken, "revenue", currencyCode)
    }

    suspend fun feeRanksSingle(authToken: String, currencyCode: String): List<RankMultiValue> {
        return hsProvider.rankMultiValueSingle(authToken, "fee", currencyCode)
    }

    suspend fun subscriptionsSingle(addresses: List<String>): List<SubscriptionResponse> {
        return hsProvider.subscriptionsSingle(addresses)
    }

    // Overview
    suspend fun marketOverviewSingle(currencyCode: String): MarketOverview =
        marketOverviewManager.marketOverviewSingle(currencyCode).let { marketOverview ->
            marketOverview.copy(
                topPairs = marketOverview.topPairs.map { topPairWithCoin(it) }
            )
        }

    private fun topPairWithCoin(topPair: TopPair) =
        topPair.copy(
            baseCoin = coinsMap[topPair.baseCoinUid],
            targetCoin = coinsMap[topPair.targetCoinUid]
        )

    suspend fun marketGlobalSingle(currencyCode: String): MarketGlobal =
        hsProvider.marketGlobalSingle(currencyCode)

    suspend fun topPairsSingle(currencyCode: String, page: Int, limit: Int): List<TopPair> =
        hsProvider.topPairsSingle(currencyCode, page, limit).map { topPairWithCoin(it) }


    suspend fun topMoversSingle(currencyCode: String): TopMovers =
        hsProvider.topMoversRawSingle(currencyCode)
            .let { raw ->
                TopMovers(
                    gainers100 = coinManager.getMarketInfos(raw.gainers100),
                    gainers200 = coinManager.getMarketInfos(raw.gainers200),
                    gainers300 = coinManager.getMarketInfos(raw.gainers300),
                    losers100 = coinManager.getMarketInfos(raw.losers100),
                    losers200 = coinManager.getMarketInfos(raw.losers200),
                    losers300 = coinManager.getMarketInfos(raw.losers300)
                )
            }

    // Chart Info

    suspend fun chartPointsSingle(
        coinUid: String,
        currencyCode: String,
        interval: HsPointTimePeriod,
        pointCount: Int
    ): List<ChartPoint> {
        val fromTimestamp = Date().time / 1000 - interval.interval * pointCount

        return hsProvider.coinPriceChartSingle(coinUid, currencyCode, interval, fromTimestamp)
            .map { chartCoinPrice ->
                chartCoinPrice.chartPoint
            }
    }

    suspend fun chartPointsSingle(
        coinUid: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): Pair<Long, List<ChartPoint>> {
        val data = intervalData(periodType)
        return hsProvider.coinPriceChartSingle(
            coinUid,
            currencyCode,
            data.interval,
            data.fromTimestamp
        )
            .let {
                Pair(data.visibleTimestamp, it.map { it.chartPoint })
            }
    }

    private fun intervalData(periodType: HsPeriodType): IntervalData {
        val interval = HsChartRequestHelper.pointInterval(periodType)
        val visibleTimestamp: Long
        val fromTimestamp: Long?
        when (periodType) {
            is HsPeriodType.ByPeriod -> {
                val currentTime = Date().time / 1000
                visibleTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
                fromTimestamp = visibleTimestamp
            }

            is HsPeriodType.ByCustomPoints -> {
                val currentTime = Date().time / 1000
                visibleTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
                val customPointsInterval = interval.interval * periodType.pointsCount
                fromTimestamp = visibleTimestamp - customPointsInterval
            }

            is HsPeriodType.ByStartTime -> {
                visibleTimestamp = periodType.startTime
                fromTimestamp = null
            }
        }

        return IntervalData(interval, fromTimestamp, visibleTimestamp)
    }

    suspend fun chartStartTimeSingle(coinUid: String): Long {
        return hsProvider.coinPriceChartStartTime(coinUid)
    }

    suspend fun topPlatformMarketCapStartTimeSingle(platform: String): Long {
        return hsProvider.topPlatformMarketCapStartTime(platform)
    }

    // Global Market Info

    suspend fun globalMarketPointsSingle(
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<GlobalMarketPoint> {
        return globalMarketInfoManager.globalMarketInfoSingle(currencyCode, timePeriod)
    }

    suspend fun topPlatformsSingle(currencyCode: String): List<TopPlatform> {
        return hsProvider.topPlatformsSingle(currencyCode).map { it.topPlatform }
    }

    suspend fun topPlatformMarketCapPointsSingle(
        chain: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): List<TopPlatformMarketCapPoint> {
        val data = intervalData(periodType)
        return hsProvider.topPlatformMarketCapPointsSingle(
            chain,
            currencyCode,
            data.interval,
            data.fromTimestamp
        )
    }

    suspend fun topPlatformMarketInfosSingle(
        chain: String,
        currencyCode: String,
    ): List<MarketInfo> {
        return coinManager.getMarketInfos(hsProvider.topPlatformCoinListSingle(chain, currencyCode))
    }

    // NFT

    suspend fun nftTopCollections(): List<NftTopCollection> = nftManager.topCollections()

    // Auth

    suspend fun authGetSignMessage(address: String): String {
        return hsProvider.authGetSignMessage(address)
    }

    suspend fun authenticate(signature: String, address: String): String {
        return hsProvider.authenticate(signature, address)
    }

    suspend fun requestPersonalSupport(authToken: String, username: String): Response<Void> {
        return hsProvider.requestPersonalSupport(authToken, username)
    }

    suspend fun requestVipSupport(authToken: String, subscriptionId: String): Map<String, String> {
        return hsProvider.requestVipSupport(authToken, subscriptionId)
    }

    //Misc

    fun syncInfo(): SyncInfo {
        return coinSyncer.syncInfo()
    }

    fun getInitialDump(): String {
        return dumpManager.getInitialDump()
    }

    suspend fun getStocks(currencyCode: String): List<Stock> {
        return hsProvider.getStocks(currencyCode)
    }

    //ETF

    suspend fun etfSingle(category: String, currencyCode: String): List<Etf> {
        return hsProvider.etfsSingle(category, currencyCode).map { EtfResponse.toEtf(it) }
    }

    suspend fun etfPointSingle(category: String, currencyCode: String, period: String): List<EtfPoint> {
        return hsProvider.etfPointsSingle(category, currencyCode, period).mapNotNull { EtfPointResponse.toEtfPoint(it) }
    }

    // Vaults
    suspend fun vaultsSingle(currencyCode: String): List<Vault> {
        return hsProvider.vaultsSingle(currencyCode)
    }

    suspend fun vaultSingle(tokenAddress: String, currencyCode: String, period: HsTimePeriod = HsTimePeriod.Month1): Vault {
        return hsProvider.vaultSingle(tokenAddress, currencyCode, period)
    }

    //Stats

    suspend fun sendStats(statsJson: String, appVersion: String, appId: String?): Unit {
        return hsProvider.sendStats(statsJson, appVersion, appId)
    }

    companion object {
        fun getInstance(
            context: Context,
            hsApiBaseUrl: String,
            hsApiKey: String,
            newsApiKey: String,
        ): MarketKit {
            // init cache
            (context.getSystemService(Context.STORAGE_SERVICE) as StorageManager?)?.let { storageManager ->
                val cacheDir = context.cacheDir
                val cacheQuotaBytes =
                    storageManager.getCacheQuotaBytes(storageManager.getUuidForPath(cacheDir))

                HSCache.cacheDir = cacheDir
                HSCache.cacheQuotaBytes = cacheQuotaBytes
            }

            val marketDatabase = MarketDatabase.getInstance(context)
            val dumpManager = DumpManager(marketDatabase)
            val hsProvider = HsProvider(hsApiBaseUrl, hsApiKey)
            val hsNftProvider = HsNftProvider(hsApiBaseUrl, hsApiKey)
            val coinStorage = CoinStorage(marketDatabase)
            val coinManager = CoinManager(coinStorage)
            val nftManager = NftManager(coinManager, hsNftProvider)
            val marketOverviewManager = MarketOverviewManager(nftManager, hsProvider)
            val coinSyncer = CoinSyncer(hsProvider, coinStorage, marketDatabase.syncerStateDao())
            val coinPriceManager = CoinPriceManager(CoinPriceStorage(marketDatabase))
            val coinHistoricalPriceManager = CoinHistoricalPriceManager(
                CoinHistoricalPriceStorage(marketDatabase),
                hsProvider,
            )
            val coinPriceSchedulerFactory = CoinPriceSchedulerFactory(coinPriceManager, hsProvider)
            val coinPriceSyncManager = CoinPriceSyncManager(coinPriceSchedulerFactory)
            coinPriceManager.listener = coinPriceSyncManager
            val cryptoCompareProvider = CryptoCompareProvider(newsApiKey)
            val postManager = PostManager(cryptoCompareProvider)
            val globalMarketInfoStorage = GlobalMarketInfoStorage(marketDatabase)
            val globalMarketInfoManager =
                GlobalMarketInfoManager(hsProvider, globalMarketInfoStorage)
            val hsDataSyncer = HsDataSyncer(coinSyncer, hsProvider)

            return MarketKit(
                nftManager,
                marketOverviewManager,
                coinManager,
                coinSyncer,
                coinPriceManager,
                coinHistoricalPriceManager,
                coinPriceSyncManager,
                postManager,
                globalMarketInfoManager,
                hsProvider,
                hsDataSyncer,
                dumpManager,
            )
        }
    }

}

//Errors

sealed class ProviderError : Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class ReturnedTimestampIsVeryInaccurate : ProviderError()
}

class SyncInfo(
    val coinsTimestamp: String?,
    val blockchainsTimestamp: String?,
    val tokensTimestamp: String?
)
