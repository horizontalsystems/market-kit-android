package io.horizontalsystems.marketkit.providers

import com.google.gson.annotations.SerializedName
import io.horizontalsystems.marketkit.models.Analytics
import io.horizontalsystems.marketkit.models.AnalyticsPreview
import io.horizontalsystems.marketkit.models.BlockchainResponse
import io.horizontalsystems.marketkit.models.Category
import io.horizontalsystems.marketkit.models.ChartPoint
import io.horizontalsystems.marketkit.models.CoinCategory
import io.horizontalsystems.marketkit.models.CoinCategoryMarketPoint
import io.horizontalsystems.marketkit.models.CoinInvestment
import io.horizontalsystems.marketkit.models.CoinPrice
import io.horizontalsystems.marketkit.models.CoinPriceResponse
import io.horizontalsystems.marketkit.models.CoinReport
import io.horizontalsystems.marketkit.models.CoinResponse
import io.horizontalsystems.marketkit.models.CoinTreasury
import io.horizontalsystems.marketkit.models.CoinTreasuryResponse
import io.horizontalsystems.marketkit.models.DefiMarketInfoResponse
import io.horizontalsystems.marketkit.models.EtfPointResponse
import io.horizontalsystems.marketkit.models.EtfResponse
import io.horizontalsystems.marketkit.models.GlobalMarketPoint
import io.horizontalsystems.marketkit.models.HsPointTimePeriod
import io.horizontalsystems.marketkit.models.HsStatus
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.horizontalsystems.marketkit.models.MarketGlobal
import io.horizontalsystems.marketkit.models.MarketInfoDetailsResponse
import io.horizontalsystems.marketkit.models.MarketInfoOverview
import io.horizontalsystems.marketkit.models.MarketInfoOverviewRaw
import io.horizontalsystems.marketkit.models.MarketInfoRaw
import io.horizontalsystems.marketkit.models.MarketInfoTvlResponse
import io.horizontalsystems.marketkit.models.MarketOverviewResponse
import io.horizontalsystems.marketkit.models.MarketTicker
import io.horizontalsystems.marketkit.models.RankMultiValue
import io.horizontalsystems.marketkit.models.RankValue
import io.horizontalsystems.marketkit.models.Stock
import io.horizontalsystems.marketkit.models.SubscriptionResponse
import io.horizontalsystems.marketkit.models.TokenHolders
import io.horizontalsystems.marketkit.models.TokenResponse
import io.horizontalsystems.marketkit.models.TopMoversRaw
import io.horizontalsystems.marketkit.models.TopPair
import io.horizontalsystems.marketkit.models.TopPlatformMarketCapPoint
import io.horizontalsystems.marketkit.models.TopPlatformResponse
import io.horizontalsystems.marketkit.models.Vault
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

class HsProvider(baseUrl: String, apiKey: String) {

    private val service by lazy {
        RetrofitUtils.build("${baseUrl}/v1/", mapOf("apikey" to apiKey))
            .create(MarketService::class.java)
    }

    suspend fun marketInfosSingle(
        top: Int,
        currencyCode: String,
        defi: Boolean,
    ): List<MarketInfoRaw> {
        return service.getMarketInfos(
            top = top,
            currencyCode = currencyCode,
            defi = defi
        )
    }

    suspend fun advancedMarketInfosSingle(
        top: Int,
        currencyCode: String,
    ): List<MarketInfoRaw> {
        return service.getAdvancedMarketInfos(
            top = top,
            currencyCode = currencyCode,
        )
    }

    suspend fun marketInfosSingle(
        coinUids: List<String>,
        currencyCode: String,
    ): List<MarketInfoRaw> {
        return service.getMarketInfos(
            uids = coinUids.joinToString(","),
            currencyCode = currencyCode
        )
    }

    suspend fun topCoinsMarketInfosSingle(
        top: Int,
        currencyCode: String,
    ): List<MarketInfoRaw> {
        return service.getTopCoinsMarketInfos(
            top = top,
            currencyCode = currencyCode,
        )
    }

    suspend fun categoriesSingle(): List<Category> {
        return service.getCategories()
    }

    suspend fun marketInfosSingle(
        categoryUid: String,
        currencyCode: String,
    ): List<MarketInfoRaw> {
        return service.getMarketInfosByCategory(
            categoryUid = categoryUid,
            currencyCode = currencyCode
        )
    }

    suspend fun getCoinCategories(currencyCode: String): List<CoinCategory> {
        return service.getCategories(currencyCode)
    }

    suspend fun coinCategoryMarketPointsSingle(
        categoryUid: String,
        timePeriod: HsTimePeriod,
        currencyCode: String,
    ): List<CoinCategoryMarketPoint> {
        return service.coinCategoryMarketPoints(categoryUid, timePeriod.value, currencyCode)
    }

    suspend fun getCoinPrices(
        coinUids: List<String>,
        currencyCode: String
    ): List<CoinPrice> {
        return service.getCoinPrices(
            uids = coinUids.joinToString(separator = ","),
            currencyCode = currencyCode,
        ).mapNotNull { coinPriceResponse ->
            coinPriceResponse.coinPrice(currencyCode)
        }
    }

    suspend fun historicalCoinPriceSingle(
        coinUid: String,
        currencyCode: String,
        timestamp: Long
    ): HistoricalCoinPriceResponse {
        return service.getHistoricalCoinPrice(coinUid, currencyCode, timestamp)
    }

    suspend fun coinPriceChartSingle(
        coinUid: String,
        currencyCode: String,
        periodType: HsPointTimePeriod,
        fromTimestamp: Long?
    ): List<ChartCoinPriceResponse> {
        return service.getCoinPriceChart(coinUid, currencyCode, fromTimestamp, periodType.value)
    }

    suspend fun coinPriceChartStartTime(coinUid: String): Long {
        return service.getCoinPriceChartStart(coinUid).timestamp
    }

    suspend fun topPlatformMarketCapStartTime(platform: String): Long {
        return service.getTopPlatformMarketCapStart(platform).timestamp
    }

    suspend fun getMarketInfoOverview(
        coinUid: String,
        currencyCode: String,
        language: String,
        roiUids: List<String>,
        roiPeriods: List<HsTimePeriod>,
    ): MarketInfoOverviewRaw {
        return service.getMarketInfoOverview(
            coinUid = coinUid,
            currencyCode = currencyCode,
            language = language,
            roiUids = roiUids.ifEmpty { null }
                ?.joinToString(","),
            roiPeriods = roiPeriods.ifEmpty { null }
                ?.map { MarketInfoOverview.hsTimePeriodToStr(it) }
                ?.joinToString(",")
        )
    }

    suspend fun getGlobalMarketPointsSingle(
        currencyCode: String,
        timePeriod: HsTimePeriod,
    ): List<GlobalMarketPoint> {
        return service.globalMarketPoints(timePeriod.value, currencyCode)
    }

    suspend fun defiMarketInfosSingle(currencyCode: String): List<DefiMarketInfoResponse> {
        return service.getDefiMarketInfos(currencyCode = currencyCode)
    }

    suspend fun marketInfoTvlSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<ChartPoint> {
        return service.getMarketInfoTvl(coinUid, currencyCode, timePeriod.value)
            .mapNotNull {
                it.tvl?.let { tvl -> ChartPoint(tvl, it.timestamp, null) }
            }
    }

    suspend fun marketInfoGlobalTvlSingle(
        chain: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<ChartPoint> {

        return service.getMarketInfoGlobalTvl(
            currencyCode,
            timePeriod.value,
            blockchain = if (chain.isNotBlank()) chain else null
        ).mapNotNull {
            it.tvl?.let { tvl ->
                ChartPoint(tvl, it.timestamp, null)
            }
        }
    }

    suspend fun tokenHoldersSingle(
        authToken: String,
        coinUid: String,
        blockchainUid: String
    ): TokenHolders {
        return service.getTokenHolders(authToken, coinUid, blockchainUid)
    }

    suspend fun coinTreasuriesSingle(coinUid: String, currencyCode: String): List<CoinTreasury> {
        return service.getCoinTreasuries(coinUid, currencyCode).mapNotNull {
            try {
                CoinTreasury(
                    type = CoinTreasury.TreasuryType.fromString(it.type)!!,
                    fund = it.fund,
                    fundUid = it.fundUid,
                    amount = it.amount,
                    amountInCurrency = it.amountInCurrency,
                    countryCode = it.countryCode
                )
            } catch (exception: Exception) {
                null
            }
        }
    }

    suspend fun investmentsSingle(coinUid: String): List<CoinInvestment> {
        return service.getInvestments(coinUid)
    }

    suspend fun coinReportsSingle(coinUid: String): List<CoinReport> {
        return service.getCoinReports(coinUid)
    }

    suspend fun topPlatformsSingle(currencyCode: String): List<TopPlatformResponse> {
        return service.getTopPlatforms(currencyCode = currencyCode)
    }

    suspend fun topPlatformMarketCapPointsSingle(
        chain: String,
        currencyCode: String,
        periodType: HsPointTimePeriod,
        fromTimestamp: Long?
    ): List<TopPlatformMarketCapPoint> {
        return service.getTopPlatformMarketCapPoints(chain, currencyCode, fromTimestamp, periodType.value)
    }

    suspend fun topPlatformCoinListSingle(
        chain: String,
        currencyCode: String
    ): List<MarketInfoRaw> {
        return service.getTopPlatformCoinList(
            chain = chain,
            currencyCode = currencyCode
        )
    }

    suspend fun dexLiquiditySingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<Analytics.VolumePoint> {
        return service.getDexLiquidities(authToken, coinUid, currencyCode, timePeriod.value)
    }

    suspend fun dexVolumesSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): List<Analytics.VolumePoint> {
        return service.getDexVolumes(authToken, coinUid, currencyCode, timePeriod.value)
    }

    suspend fun transactionDataSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod,
        platform: String?
    ): List<Analytics.CountVolumePoint> {
        return service.getTransactions(authToken, coinUid, timePeriod.value, platform)
    }

    suspend fun activeAddressesSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod
    ): List<Analytics.CountPoint> {
        return service.getActiveAddresses(authToken, coinUid, timePeriod.value)
    }

    suspend fun marketOverviewSingle(currencyCode: String): MarketOverviewResponse {
        return service.getMarketOverview(currencyCode)
    }

    suspend fun marketGlobalSingle(currencyCode: String): MarketGlobal {
        return service.getMarketGlobal(currencyCode)
    }

    suspend fun marketTickers(coinUid: String, currencyCode: String): List<MarketTicker> {
        return service.getMarketTickers(coinUid, currencyCode)
    }

    suspend fun topMoversRawSingle(currencyCode: String): TopMoversRaw {
        return service.getTopMovers(currencyCode)
    }

    suspend fun statusSingle(): HsStatus {
        return service.getStatus()
    }

    suspend fun allCoinsSingle(): List<CoinResponse> {
        return service.getAllCoins()
    }

    suspend fun allBlockchainsSingle(): List<BlockchainResponse> {
        return service.getAllBlockchains()
    }

    suspend fun allTokensSingle(): List<TokenResponse> {
        return service.getAllTokens()
    }

    suspend fun analyticsPreviewSingle(coinUid: String, addresses: List<String>): AnalyticsPreview {
        return service.getAnalyticsPreview(
            coinUid = coinUid,
            address = if (addresses.isEmpty()) null else addresses.joinToString(",")
        )
    }

    suspend fun analyticsSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
    ): Analytics {
        return service.getAnalyticsData(
            authToken = authToken,
            coinUid = coinUid,
            currencyCode = currencyCode
        )
    }

    suspend fun rankValueSingle(
        authToken: String,
        type: String,
        currencyCode: String
    ): List<RankValue> {
        return service.getRankValue(authToken, type, currencyCode)
    }

    suspend fun rankMultiValueSingle(
        authToken: String,
        type: String,
        currencyCode: String
    ): List<RankMultiValue> {
        return service.getRankMultiValue(authToken, type, currencyCode)
    }

    suspend fun subscriptionsSingle(
        addresses: List<String>
    ): List<SubscriptionResponse> {
        return service.getSubscriptions(addresses.joinToString(separator = ","))
    }

    suspend fun authGetSignMessage(address: String): String {
        return service.authGetSignMessage(address)["message"]
            ?: error("Response has no 'message' field")
    }

    suspend fun authenticate(signature: String, address: String): String {
        return service.authenticate(signature, address)["token"]
            ?: error("Response has no 'token' field")
    }

    suspend fun requestPersonalSupport(authToken: String, username: String): Response<Void> {
        return service.requestPersonalSupport(authToken, username)
    }

    suspend fun requestVipSupport(authToken: String, subscriptionId: String): Map<String, String> {
        return service.requestVipSupport(authToken, subscriptionId)
    }

    suspend fun verifiedExchangeUids(): List<String> {
        return service.verifiedExchangeUids()
    }

    suspend fun topPairsSingle(currencyCode: String, page: Int, limit: Int): List<TopPair> {
        return service.getTopPairs(currencyCode, page, limit)
    }

    suspend fun sendStats(statsJson: String, appVersion: String, appId: String?): Unit {
        return service.sendStats(
            appPlatform = "android",
            appVersion = appVersion,
            appId = appId,
            stats = statsJson
        )
    }

    suspend fun coinsSignalsSingle(uids: List<String>): List<SignalResponse> {
        return service.getCoinsSignals(uids.joinToString(separator = ","))
    }

    suspend fun etfsSingle(category: String, currencyCode: String): List<EtfResponse> {
        return service.getEtfs(category, currencyCode)
    }

    suspend fun etfPointsSingle(category: String, currencyCode: String, period: String): List<EtfPointResponse> {
        return service.getEtfPoints(category, currencyCode, period)
    }

    suspend fun vaultsSingle(currencyCode: String): List<Vault> {
        return service.getVaults(currencyCode)
    }

    suspend fun vaultSingle(tokenAddress: String, currencyCode: String, period: HsTimePeriod): Vault {
        return service.getVault(tokenAddress, currencyCode, period.value)
    }

    suspend fun getStocks(currencyCode: String): List<Stock> {
        return service.getStocks(currencyCode)
    }

    private interface MarketService {

        @GET("coins")
        suspend fun getMarketInfos(
            @Query("limit") top: Int,
            @Query("currency") currencyCode: String,
            @Query("defi") defi: Boolean,
            @Query("order_by_rank") orderByRank: Boolean = true,
            @Query("fields") fields: String = marketInfoFields,
        ): List<MarketInfoRaw>

        @GET("coins")
        suspend fun getTopCoinsMarketInfos(
            @Query("limit") top: Int,
            @Query("currency") currencyCode: String,
            @Query("order_by_rank") orderByRank: Boolean = true,
            @Query("fields") fields: String = topCoinsMarketInfoFields,
        ): List<MarketInfoRaw>

        @GET("coins/filter")
        suspend fun getAdvancedMarketInfos(
            @Query("limit") top: Int,
            @Query("currency") currencyCode: String,
            @Query("order_by_rank") orderByRank: Boolean = true,
            @Query("page") page: Int = 1,
        ): List<MarketInfoRaw>

        @GET("coins")
        suspend fun getMarketInfos(
            @Query("uids") uids: String,
            @Query("currency") currencyCode: String,
            @Query("fields") fields: String = marketInfoFields,
        ): List<MarketInfoRaw>

        @GET("categories/")
        suspend fun getCategories(): List<Category>

        @GET("categories/{categoryUid}/coins")
        suspend fun getMarketInfosByCategory(
            @Path("categoryUid") categoryUid: String,
            @Query("currency") currencyCode: String,
        ): List<MarketInfoRaw>

        @GET("categories/with-top-coins")
        suspend fun getCategories(
            @Query("currency") currencyCode: String
        ): List<CoinCategory>

        @GET("categories/{categoryUid}/market_cap")
        suspend fun coinCategoryMarketPoints(
            @Path("categoryUid") categoryUid: String,
            @Query("interval") interval: String,
            @Query("currency") currencyCode: String,
        ): List<CoinCategoryMarketPoint>

        @GET("coins")
        suspend fun getCoinPrices(
            @Query("uids") uids: String,
            @Query("currency") currencyCode: String,
            @Query("fields") fields: String = coinPriceFields,
        ): List<CoinPriceResponse>

        @GET("coins/{coinUid}/price_history")
        suspend fun getHistoricalCoinPrice(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("timestamp") timestamp: Long,
        ): HistoricalCoinPriceResponse

        @GET("coins/{coinUid}/price_chart")
        suspend fun getCoinPriceChart(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("from_timestamp") timestamp: Long?,
            @Query("interval") interval: String,
        ): List<ChartCoinPriceResponse>

        @GET("coins/{coinUid}/price_chart_start")
        suspend fun getCoinPriceChartStart(
            @Path("coinUid") coinUid: String
        ): ChartStart

        @GET("coins/{coinUid}")
        suspend fun getMarketInfoOverview(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("language") language: String,
            @Query("roi_uids") roiUids: String?,
            @Query("roi_periods") roiPeriods: String?,
        ): MarketInfoOverviewRaw

        @GET("defi-protocols")
        suspend fun getDefiMarketInfos(
            @Query("currency") currencyCode: String
        ): List<DefiMarketInfoResponse>

        @GET("coins/{coinUid}/details")
        suspend fun getMarketInfoDetails(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String
        ): MarketInfoDetailsResponse

        @GET("analytics/{coinUid}/preview")
        suspend fun getAnalyticsPreview(
            @Path("coinUid") coinUid: String,
            @Query("address") address: String?,
        ): AnalyticsPreview

        @GET("analytics/{coinUid}")
        suspend fun getAnalyticsData(
            @Header("authorization") authToken: String,
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
        ): Analytics

        @GET("analytics/{coinUid}/dex-liquidity")
        suspend fun getDexLiquidities(
            @Header("authorization") auth: String,
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("interval") interval: String,
        ): List<Analytics.VolumePoint>

        @GET("analytics/{coinUid}/dex-volumes")
        suspend fun getDexVolumes(
            @Header("authorization") auth: String,
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("interval") interval: String
        ): List<Analytics.VolumePoint>

        @GET("analytics/{coinUid}/transactions")
        suspend fun getTransactions(
            @Header("authorization") auth: String,
            @Path("coinUid") coinUid: String,
            @Query("interval") interval: String,
            @Query("platform") platform: String?
        ): List<Analytics.CountVolumePoint>

        @GET("analytics/{coinUid}/addresses")
        suspend fun getActiveAddresses(
            @Header("authorization") auth: String,
            @Path("coinUid") coinUid: String,
            @Query("interval") interval: String
        ): List<Analytics.CountPoint>

        @GET("analytics/{coinUid}/holders")
        suspend fun getTokenHolders(
            @Header("authorization") authToken: String,
            @Path("coinUid") coinUid: String,
            @Query("blockchain_uid") blockchainUid: String
        ): TokenHolders

        @GET("analytics/ranks")
        suspend fun getRankValue(
            @Header("authorization") authToken: String,
            @Query("type") type: String,
            @Query("currency") currencyCode: String,
        ): List<RankValue>

        @GET("analytics/ranks")
        suspend fun getRankMultiValue(
            @Header("authorization") authToken: String,
            @Query("type") type: String,
            @Query("currency") currencyCode: String,
        ): List<RankMultiValue>

        @GET("analytics/subscriptions")
        suspend fun getSubscriptions(
            @Query("address") addresses: String
        ): List<SubscriptionResponse>

        @GET("defi-protocols/{coinUid}/tvls")
        suspend fun getMarketInfoTvl(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("interval") interval: String
        ): List<MarketInfoTvlResponse>

        @GET("global-markets/tvls")
        suspend fun getMarketInfoGlobalTvl(
            @Query("currency") currencyCode: String,
            @Query("interval") interval: String,
            @Query("blockchain") blockchain: String?
        ): List<MarketInfoTvlResponse>

        @GET("funds/treasuries")
        suspend fun getCoinTreasuries(
            @Query("coin_uid") coinUid: String,
            @Query("currency") currencyCode: String
        ): List<CoinTreasuryResponse>

        @GET("funds/investments")
        suspend fun getInvestments(
            @Query("coin_uid") coinUid: String,
        ): List<CoinInvestment>

        @GET("reports")
        suspend fun getCoinReports(
            @Query("coin_uid") coinUid: String
        ): List<CoinReport>

        @GET("global-markets")
        suspend fun globalMarketPoints(
            @Query("interval") timePeriod: String,
            @Query("currency") currencyCode: String,
        ): List<GlobalMarketPoint>

        @GET("top-platforms")
        suspend fun getTopPlatforms(
            @Query("currency") currencyCode: String
        ): List<TopPlatformResponse>

        @GET("top-platforms/{platform}/market_chart_start")
        suspend fun getTopPlatformMarketCapStart(
            @Path("platform") platform: String
        ): ChartStart

        @GET("top-platforms/{platform}/market_chart")
        suspend fun getTopPlatformMarketCapPoints(
            @Path("platform") platform: String,
            @Query("currency") currencyCode: String,
            @Query("from_timestamp") timestamp: Long?,
            @Query("interval") interval: String
        ): List<TopPlatformMarketCapPoint>

        @GET("top-platforms/{chain}/list")
        suspend fun getTopPlatformCoinList(
            @Path("chain") chain: String,
            @Query("currency") currencyCode: String,
        ): List<MarketInfoRaw>

        @GET("markets/overview")
        suspend fun getMarketOverview(
            @Query("currency") currencyCode: String,
            @Query("simplified") simplified: Boolean = true
        ): MarketOverviewResponse

        @GET("markets/overview-simple")
        suspend fun getMarketGlobal(
            @Query("currency") currencyCode: String
        ): MarketGlobal

        @GET("exchanges/tickers/{coinUid}")
        suspend fun getMarketTickers(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
        ): List<MarketTicker>

        @GET("coins/top-movers")
        suspend fun getTopMovers(
            @Query("currency") currencyCode: String
        ): TopMoversRaw

        @GET("status/updates")
        suspend fun getStatus(): HsStatus

        @GET("coins/list")
        suspend fun getAllCoins(): List<CoinResponse>

        @GET("blockchains/list")
        suspend fun getAllBlockchains(): List<BlockchainResponse>

        @GET("tokens/list")
        suspend fun getAllTokens(): List<TokenResponse>

        @GET("auth/get-sign-message")
        suspend fun authGetSignMessage(
            @Query("address") address: String
        ): Map<String, String>

        @FormUrlEncoded
        @POST("auth/authenticate")
        suspend fun authenticate(
            @Field("signature") signature: String,
            @Field("address") address: String
        ): Map<String, String>

        @FormUrlEncoded
        @POST("support/start-chat")
        suspend fun requestPersonalSupport(
            @Header("authorization") auth: String,
            @Field("username") username: String,
        ): Response<Void>

        @FormUrlEncoded
        @POST("support/create-group")
        suspend fun requestVipSupport(
            @Header("authorization") auth: String,
            @Field("subscription_id") subscriptionId: String,
            @Field("platform") platform: String = "android",
        ): Map<String, String>

        @GET("exchanges/whitelist")
        suspend fun verifiedExchangeUids(): List<String>

        @GET("exchanges/top-market-pairs")
        suspend fun getTopPairs(
            @Query("currency") currencyCode: String,
            @Query("page") page: Int,
            @Query("limit") limit: Int
        ): List<TopPair>

        @POST("stats")
        @Headers("Content-Type: application/json")
        suspend fun sendStats(
            @Header("app_platform") appPlatform: String,
            @Header("app_version") appVersion: String,
            @Header("app_id") appId: String?,
            @Body stats: String,
        ): Unit

        @GET("coins/signals")
        suspend fun getCoinsSignals(
            @Query("uids") uids: String,
        ): List<SignalResponse>

        @GET("etfs/all")
        suspend fun getEtfs(
            @Query("category") category: String,
            @Query("currency") currencyCode: String,
        ): List<EtfResponse>

        @GET("etfs/chart")
        suspend fun getEtfPoints(
            @Query("category") category: String,
            @Query("currency") currencyCode: String,
            @Query("interval") interval: String,
        ): List<EtfPointResponse>

        @GET("vaults")
        suspend fun getVaults(
            @Query("currency") currencyCode: String
        ): List<Vault>

        @GET("vaults/{tokenAddress}")
        suspend fun getVault(
            @Path("tokenAddress") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("range_interval") interval: String,
        ): Vault

        @GET("stocks")
        suspend fun getStocks(
            @Query("currency") currencyCode: String,
        ): List<Stock>

        companion object {
            private const val marketInfoFields =
                "name,code,price,price_change_1d,price_change_24h,price_change_7d,price_change_30d,price_change_90d,market_cap_rank,coingecko_id,market_cap,market_cap_rank,total_volume"
            private const val topCoinsMarketInfoFields =
                "price,price_change_1d,price_change_24h,price_change_7d,price_change_30d,price_change_90d,market_cap_rank,market_cap,total_volume"
            private const val coinPriceFields = "price,price_change_1d,price_change_24h,last_updated"
            private const val advancedMarketFields =
                "all_platforms,price,market_cap,total_volume,price_change_1d,price_change_24h,price_change_7d,price_change_14d,price_change_30d,price_change_200d,price_change_1y,ath_percentage,atl_percentage"
        }
    }
}

data class HistoricalCoinPriceResponse(
    val timestamp: Long,
    val price: BigDecimal,
)

data class SignalResponse(
    val uid: String,
    val signal: Analytics.TechnicalAdvice.Advice?
)

data class ChartStart(val timestamp: Long)

data class ChartCoinPriceResponse(
    val timestamp: Long,
    val price: BigDecimal,
    @SerializedName("volume")
    val totalVolume: BigDecimal?
) {
    val chartPoint: ChartPoint
        get() {
            return ChartPoint(
                price,
                timestamp,
                totalVolume
            )
        }
}
