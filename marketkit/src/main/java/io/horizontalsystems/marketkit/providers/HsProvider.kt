package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.models.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class HsProvider(
    baseUrl: String,
    oldBaseUrl: String
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

    fun getMarketInfosSingle(top: Int, limit: Int?, order: MarketInfo.Order?): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(top, limit, order?.field?.v, order?.direction?.v)
    }

    fun getMarketInfosSingle(coinUids: List<String>, order: MarketInfo.Order?): Single<List<MarketInfoRaw>> {
        return service.getMarketInfos(coinUids.joinToString(separator = ","), order?.field?.v, order?.direction?.v)
    }

    fun getCoinCategories(): Single<List<CoinCategory>> {
        return service.getCategories()
    }

    fun getCoinPrices(coinUids: List<String>, currencyCode: String): Single<List<CoinPrice>> {
        return service.getCoinPrices(coinUids.joinToString(separator = ","), currencyCode.lowercase())
            .map { coinPricesMap ->
                coinPricesMap.map { (coinUid, coinPriceResponse) ->
                    coinPriceResponse.coinPrice(coinUid, currencyCode)
                }
            }
    }

    fun getMarketInfoOverview(coinUid: String, currencyCode: String, language: String): Single<MarketInfoOverviewRaw> {
        return service.getMarketInfoOverview(coinUid, currencyCode, language)
    }

    fun getGlobalMarketPointsSingle(currencyCode: String, timePeriod: TimePeriod): Single<List<GlobalMarketPoint>> {
        return serviceOld.globalMarketPoints(timePeriod.v, currencyCode)
    }

    private interface MarketService {
        @GET("coins")
        fun getFullCoins(): Single<List<FullCoinResponse>>

        @GET("coins/top_markets")
        fun getMarketInfos(
            @Query("top") top: Int,
            @Query("limit") limit: Int?,
            @Query("orderField") orderField: String?,
            @Query("orderDirection") orderDirection: String?,
        ): Single<List<MarketInfoRaw>>

        @GET("coins/markets")
        fun getMarketInfos(
            @Query("uids") uids: String,
            @Query("orderField") orderField: String?,
            @Query("orderDirection") orderDirection: String?
        ): Single<List<MarketInfoRaw>>

        @GET("categories")
        fun getCategories(): Single<List<CoinCategory>>

        @GET("coins/markets_prices")
        fun getCoinPrices(
            @Query("uids") uids: String,
            @Query("currency") currencyCode: String
        ): Single<Map<String, CoinPriceResponse>>

        @GET("coins/{coinUid}")
        fun getMarketInfoOverview(
            @Path("coinUid") coinUid: String,
            @Query("currency") currency: String,
            @Query("language") language: String,
        ): Single<MarketInfoOverviewRaw>
    }

    private interface MarketServiceOld {
        @GET("markets/global/{timePeriod}")
        fun globalMarketPoints(
            @Path("timePeriod") timePeriod: String,
            @Query("currency_code") currencyCode: String
        ): Single<List<GlobalMarketPoint>>
    }
}
