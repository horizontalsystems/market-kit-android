package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.models.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class HsProvider(baseUrl: String, apiKey: String) {

    private val service by lazy {
        RetrofitUtils.build("${baseUrl}/v1/", mapOf("apikey" to apiKey)).create(MarketService::class.java)
    }

    fun getFullCoins(page: Int, limit: Int): Single<List<FullCoin>> {
        return service.getFullCoins(page, limit)
            .map { responseCoinsList ->
                responseCoinsList.map { it.fullCoin() }
            }
    }

    fun marketInfosSingle(top: Int, currencyCode: String, defi: Boolean): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(top, currencyCode, defi)
    }

    fun advancedMarketInfosSingle(top: Int, currencyCode: String): Single<List<MarketInfoRaw>> {
        return service.getAdvancedMarketInfos(top, currencyCode)
    }

    fun marketInfosSingle(coinUids: List<String>, currencyCode: String): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(coinUids.joinToString(","), currencyCode)
    }

    fun marketInfosSingle(categoryUid: String, currencyCode: String): Single<List<MarketInfoRaw>> {
        return service.getMarketInfosByCategory(categoryUid, currencyCode)
    }

    fun getCoinCategories(): Single<List<CoinCategory>> {
        return service.getCategories()
    }

    fun getCoinPrices(coinUids: List<String>, currencyCode: String): Single<List<CoinPrice>> {
        return service.getCoinPrices(coinUids.joinToString(separator = ","), currencyCode)
            .map { coinPrices ->
                coinPrices.mapNotNull { coinPriceResponse ->
                    coinPriceResponse.coinPrice(currencyCode)
                }
            }
    }

    fun getMarketInfoOverview(
        coinUid: String,
        currencyCode: String,
        language: String,
    ): Single<MarketInfoOverviewRaw> {
        return service.getMarketInfoOverview(coinUid, currencyCode, language)
    }

    fun getGlobalMarketPointsSingle(
        currencyCode: String,
        timePeriod: TimePeriod,
    ): Single<List<GlobalMarketPoint>> {
        val interval = when (timePeriod) {
            TimePeriod.Day7 -> "7d"
            TimePeriod.Day30 -> "30d"
            else -> "1d"
        }

        return service.globalMarketPoints(interval, currencyCode)
    }

    fun defiMarketInfosSingle(currencyCode: String): Single<List<DefiMarketInfoResponse>> {
        return service.getDefiMarketInfos(currencyCode)
    }

    fun getMarketInfoDetails(coinUid: String, currency: String): Single<MarketInfoDetailsResponse> {
        return service.getMarketInfoDetails(coinUid, currency)
    }

    fun marketInfoTvlSingle(coinUid: String, currencyCode: String, timePeriod: TimePeriod): Single<List<ChartPoint>> {
        val interval = when (timePeriod) {
            TimePeriod.Day7 -> "7d"
            TimePeriod.Day30 -> "30d"
            else -> "1d"
        }
        return service.getMarketInfoTvl(coinUid, currencyCode, interval).map { responseList ->
            responseList.mapNotNull { it.tvl?.let { tvl -> ChartPoint(tvl, null, it.timestamp) } }
        }
    }

    fun marketInfoGlobalTvlSingle(
        chain: String,
        currencyCode: String,
        timePeriod: TimePeriod
    ): Single<List<ChartPoint>> {
        val interval = when (timePeriod) {
            TimePeriod.Day7 -> "7d"
            TimePeriod.Day30 -> "30d"
            else -> "1d"
        }

        return service.getMarketInfoGlobalTvl(
            currencyCode,
            interval,
            chain = if (chain.isNotBlank()) chain else null
        ).map { responseList ->
            responseList.mapNotNull { it.tvl?.let { tvl -> ChartPoint(tvl, null, it.timestamp) } }
        }
    }

    fun topHoldersSingle(coinUid: String): Single<List<TokenHolder>> {
        return service.getTopHolders(coinUid)
    }

    fun coinTreasuriesSingle(coinUid: String, currencyCode: String): Single<List<CoinTreasury>> {
        return service.getCoinTreasuries(coinUid, currencyCode).map { responseList ->
            responseList.mapNotNull {
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
    }

    fun investmentsSingle(coinUid: String): Single<List<CoinInvestment>> {
        return service.getInvestments(coinUid)
    }

    fun coinReportsSingle(coinUid: String): Single<List<CoinReport>> {
        return service.getCoinReports(coinUid)
    }

    private interface MarketService {
        @GET("coins")
        fun getFullCoins(
            @Query("page") page: Int,
            @Query("limit") limit: Int,
            @Query("fields") fields: String = fullCoinFields,
        ): Single<List<FullCoinResponse>>

        @GET("coins")
        fun getMarketInfos(
            @Query("limit") top: Int,
            @Query("currency") currencyCode: String,
            @Query("defi") defi: Boolean,
            @Query("order_by_rank") orderByRank: Boolean = true,
            @Query("fields") fields: String = marketInfoFields,
        ): Single<List<MarketInfoRaw>>

        @GET("coins")
        fun getAdvancedMarketInfos(
            @Query("limit") top: Int,
            @Query("currency") currencyCode: String,
            @Query("order_by_rank") orderByRank: Boolean = true,
            @Query("fields") fields: String = advancedMarketFields,
        ): Single<List<MarketInfoRaw>>

        @GET("coins")
        fun getMarketInfos(
            @Query("uids") uids: String,
            @Query("currency") currencyCode: String,
            @Query("fields") fields: String = marketInfoFields,
        ): Single<List<MarketInfoRaw>>

        @GET("categories/{categoryUid}/coins")
        fun getMarketInfosByCategory(
            @Path("categoryUid") categoryUid: String,
            @Query("currency") currencyCode: String,
        ): Single<List<MarketInfoRaw>>

        @GET("categories")
        fun getCategories(): Single<List<CoinCategory>>

        @GET("coins")
        fun getCoinPrices(
            @Query("uids") uids: String,
            @Query("currency") currencyCode: String,
            @Query("fields") fields: String = coinPriceFields,
        ): Single<List<CoinPriceResponse>>

        @GET("coins/{coinUid}")
        fun getMarketInfoOverview(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("language") language: String,
        ): Single<MarketInfoOverviewRaw>

        @GET("defi-protocols")
        fun getDefiMarketInfos(
            @Query("currency") currencyCode: String
        ): Single<List<DefiMarketInfoResponse>>

        @GET("coins/{coinUid}/details")
        fun getMarketInfoDetails(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String
        ): Single<MarketInfoDetailsResponse>

        @GET("defi-protocols/{coinUid}/tvls")
        fun getMarketInfoTvl(
            @Path("coinUid") coinUid: String,
            @Query("currency") currencyCode: String,
            @Query("interval") interval: String
        ): Single<List<MarketInfoTvlResponse>>

        @GET("global-markets/tvls")
        fun getMarketInfoGlobalTvl(
            @Query("currency") currencyCode: String,
            @Query("interval") interval: String,
            @Query("chain") chain: String?
        ): Single<List<MarketInfoTvlResponse>>

        @GET("addresses/holders")
        fun getTopHolders(
            @Query("coin_uid") coinUid: String
        ): Single<List<TokenHolder>>

        @GET("funds/treasuries")
        fun getCoinTreasuries(
            @Query("coin_uid") coinUid: String,
            @Query("currency") currencyCode: String
        ): Single<List<CoinTreasuryResponse>>

        @GET("funds/investments")
        fun getInvestments(
            @Query("coin_uid") coinUid: String,
        ): Single<List<CoinInvestment>>

        @GET("reports")
        fun getCoinReports(
            @Query("coin_uid") coinUid: String
        ): Single<List<CoinReport>>

        @GET("global-markets")
        fun globalMarketPoints(
            @Query("interval") timePeriod: String,
            @Query("currency") currencyCode: String,
        ): Single<List<GlobalMarketPoint>>

        companion object {
            private const val marketInfoFields =
                "name,code,price,price_change_24h,market_cap_rank,coingecko_id,market_cap,total_volume"
            private const val fullCoinFields = "name,code,market_cap_rank,coingecko_id,platforms"
            private const val coinPriceFields = "price,price_change_24h,last_updated"
            private const val advancedMarketFields =
                "platforms,price,market_cap,total_volume,price_change_24h,price_change_7d,price_change_14d,price_change_30d,price_change_200d,price_change_1y,ath_percentage,atl_percentage"
        }
    }
}
