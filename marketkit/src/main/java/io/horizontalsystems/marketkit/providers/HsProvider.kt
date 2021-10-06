package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.models.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

class HsProvider(
    baseUrl: String
) {
    private val service = RetrofitUtils.build("${baseUrl}/v1/").create(MarketService::class.java)

    fun getFullCoins(): Single<List<FullCoin>> {
        return service.getFullCoins().map { responseCoinsList ->
            responseCoinsList.map {
                FullCoin(it)
            }
        }
    }

    fun getMarketInfosSingle(top: Int, limit: Int?, order: MarketInfo.Order?): Single<List<MarketInfo>> {
        return service.getMarketInfos(top, limit, order?.field?.v, order?.direction?.v).map {
            it.map {
                MarketInfo(it)
            }
        }
    }

    fun getMarketInfosSingle(coinUids: List<String>, order: MarketInfo.Order?): Single<List<MarketInfo>> {
        return service.getMarketInfos(coinUids.joinToString(separator = ","), order?.field?.v, order?.direction?.v)
            .map { marketInfoResponses ->
                marketInfoResponses.map { marketInfoResponse ->
                    MarketInfo(marketInfoResponse)
                }
            }
    }

    fun getCoinCategories(): Single<List<CoinCategory>> {
        return service.getCategories()
    }

    fun getCoinPrices(coinUids: List<String>, currencyCode: String): Single<List<CoinPrice>> {
        return service.getCoinPrices(coinUids.joinToString(separator = ","), currencyCode)
            .map { coinPricesMap ->
                coinPricesMap.map { (coinUid, coinPrice) ->
                    CoinPrice(coinUid, currencyCode, coinPrice.price, coinPrice.priceChange, coinPrice.lastUpdated)
                }
            }
    }

    interface MarketService {
        @GET("coins")
        fun getFullCoins(): Single<List<FullCoinResponse>>

        @GET("coins/top_markets")
        fun getMarketInfos(
            @Query("top") top: Int,
            @Query("limit") limit: Int?,
            @Query("orderField") orderField: String?,
            @Query("orderDirection") orderDirection: String?,
        ): Single<List<MarketInfoResponse>>

        @GET("coins/markets")
        fun getMarketInfos(
            @Query("uids") uids: String,
            @Query("orderField") orderField: String?,
            @Query("orderDirection") orderDirection: String?
        ): Single<List<MarketInfoResponse>>

        @GET("categories")
        fun getCategories(): Single<List<CoinCategory>>

        @GET("coins/prices")
        fun getCoinPrices(
            @Query("ids") ids: String,
            @Query("currency") currencyCode: String
        ): Single<Map<String, CoinPriceResponse>>
    }

}
