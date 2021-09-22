package io.horizontalsystems.marketkit.providers

import com.google.gson.GsonBuilder
import io.horizontalsystems.marketkit.models.CoinCategory
import io.horizontalsystems.marketkit.models.CoinResponse
import io.horizontalsystems.marketkit.models.MarketCoin
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.logging.Logger

class HsProvider {
    private val logger = Logger.getLogger("HsProvider")
    private val url = "http://161.35.110.248:3000/v1/"
    private val service: MarketService

    init {

        val loggingInterceptor =
            HttpLoggingInterceptor { message -> logger.info(message) }.setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor)

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()

        service = retrofit.create(MarketService::class.java)
    }

    fun getMarketCoins(): Single<List<MarketCoin>> {
        return service.getMarketCoins().map { responseCoinsList ->
            responseCoinsList.map {
                MarketCoin(it)
            }
        }
    }

    fun getCoinCategories(): Single<List<CoinCategory>> {
        return service.getCategories()
    }

    interface MarketService {
        @GET("coins/all")
        fun getMarketCoins(): Single<List<CoinResponse>>

        @GET("categories")
        fun getCategories(): Single<List<CoinCategory>>
    }

}
