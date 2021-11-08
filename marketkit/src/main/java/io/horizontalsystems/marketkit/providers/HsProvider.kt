package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.models.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class HsProvider(
    baseUrl: String,
    oldBaseUrl: String,
) {

    private val service by lazy {
        RetrofitUtils.build("${baseUrl}/v1/").create(MarketService::class.java)
    }

    private val serviceOld by lazy {
        RetrofitUtils.build("${oldBaseUrl}/api/v1/").create(MarketServiceOld::class.java)
    }

    fun getFullCoins(): Single<List<FullCoin>> {
        return service.getFullCoins()
            .map { responseCoinsList ->
                responseCoinsList.map { it.fullCoin() }
            }
    }

    fun marketInfosSingle(top: Int, defi: Boolean): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(top, defi = defi)
    }

    fun advancedMarketInfosSingle(top: Int, currencyCode: String): Single<List<MarketInfoRaw>> {
        return service.getAdvancedMarketInfos(top, currencyCode)
    }

    fun marketInfosSingle(coinUids: List<String>): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(coinUids.joinToString(","))
    }

    fun marketInfosSingle(categoryUid: String): Single<List<MarketInfoRaw>> {
        return service.getMarketInfosByCategory(categoryUid)
    }

    fun getCoinCategories(): Single<List<CoinCategory>> {
        return service.getCategories()
    }

    fun getCoinPrices(coinUids: List<String>, currencyCode: String): Single<List<CoinPrice>> {
        return service.getCoinPrices(coinUids.joinToString(separator = ","), currencyCode)
            .map { coinPrices ->
                coinPrices.map { coinPriceResponse ->
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
        return serviceOld.globalMarketPoints(timePeriod.v, currencyCode)
    }

    fun defiMarketInfosSingle(currencyCode: String): Single<List<DefiMarketInfoResponse>> {
        return service.getDefiMarketInfos(currencyCode)
    }

    private interface MarketService {
        @GET("coins")
        fun getFullCoins(
            @Query("fields") fields: String = fullCoinFields,
        ): Single<List<FullCoinResponse>>

        @GET("coins")
        fun getMarketInfos(
            @Query("limit") top: Int,
            @Query("fields") fields: String = marketInfoFields,
            @Query("defi") defi: Boolean
        ): Single<List<MarketInfoRaw>>

        @GET("coins")
        fun getAdvancedMarketInfos(
            @Query("limit") top: Int,
            @Query("currency") currencyCode: String,
            @Query("fields") fields: String = advancedMarketFields,
        ): Single<List<MarketInfoRaw>>

        @GET("coins")
        fun getMarketInfos(
            @Query("uids") uids: String,
            @Query("fields") fields: String = marketInfoFields,
        ): Single<List<MarketInfoRaw>>

        @GET("categories/{categoryUid}/coins")
        fun getMarketInfosByCategory(
            @Path("categoryUid") categoryUid: String,
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
            @Query("currency") currency: String,
            @Query("language") language: String,
        ): Single<MarketInfoOverviewRaw>

        @GET("defi-coins")
        fun getDefiMarketInfos(
            @Query("currency") currencyCode: String
        ): Single<List<DefiMarketInfoResponse>>

        companion object {
            private const val marketInfoFields =
                "name,code,price,price_change_24h,market_cap_rank,coingecko_id,market_cap,total_volume"
            private const val fullCoinFields = "name,code,market_cap_rank,coingecko_id,platforms"
            private const val coinPriceFields = "price,price_change_24h,last_updated"
            private const val advancedMarketFields =
                "price,market_cap,total_volume,price_change_24h,price_change_7d,price_change_14d,price_change_30d,price_change_200d,price_change_1y,ath_percentage,atl_percentage"
        }
    }

    private interface MarketServiceOld {
        @GET("markets/global/{timePeriod}")
        fun globalMarketPoints(
            @Path("timePeriod") timePeriod: String,
            @Query("currency_code") currencyCode: String,
        ): Single<List<GlobalMarketPoint>>
    }

}
