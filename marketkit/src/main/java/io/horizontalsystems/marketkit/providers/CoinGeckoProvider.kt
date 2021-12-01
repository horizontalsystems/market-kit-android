package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.ProviderError
import io.horizontalsystems.marketkit.mappers.CoinGeckoMarketChartsMapper
import io.horizontalsystems.marketkit.models.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal
import java.util.*
import kotlin.math.absoluteValue

class CoinGeckoProvider(private val baseUrl: String) {

    private val coinGeckoService: CoinGeckoService by lazy {
        RetrofitUtils.build(baseUrl).create(CoinGeckoService::class.java)
    }

    fun chartPointsSingle(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
        val externalId = chartPointKey.coin.coinGeckoId ?: throw ProviderError.NoCoinGeckoId()

        val interval = if (chartPointKey.chartType.days >= 90) "daily" else null

        return coinGeckoService.coinMarketChart(
            externalId,
            chartPointKey.currencyCode,
            chartPointKey.chartType.coinGeckoDaysParameter,
            interval
        ).map { chartPointsResponse ->
            val intervalInSeconds = chartPointKey.chartType.seconds
            val mapper = CoinGeckoMarketChartsMapper(intervalInSeconds)

            val chartPoints = mapper.map(chartPointsResponse, chartPointKey)

            if (chartPoints.size <= coinGeckoPointCount(chartPointKey.chartType)) {
                return@map chartPoints
            }

            val result = mutableListOf<ChartPointEntity>()

            val hour4: Long = 4 * 60 * 60
            val hour8: Long = hour4 * 2

            val last = chartPoints.last()
            var nextTs = when (chartPointKey.chartType.seconds) {
                hour4 -> last.timestamp - (last.timestamp % hour4)
                hour8 -> last.timestamp - (last.timestamp % hour8)
                else -> Long.MAX_VALUE
            }

            val chartHasVolumeData = chartPointKey.chartType.resource == "histoday"

            if (!chartHasVolumeData) {
                chartPoints.reversed().forEach { point ->
                    if (point.timestamp <= nextTs) {
                        result.add(point.copy(volume = null))

                        nextTs = point.timestamp - chartPointKey.chartType.seconds
                    }
                }
            } else {
                var aggregatedVolume = BigDecimal.ZERO
                var tmpPoint: ChartPointEntity? = null

                chartPoints.reversed().forEach { point ->
                    if (point.timestamp <= nextTs) {
                        tmpPoint?.let {
                            result.add(it.copy(volume = aggregatedVolume))
                        }

                        tmpPoint = point
                        nextTs = point.timestamp - chartPointKey.chartType.seconds
                        aggregatedVolume = BigDecimal.ZERO
                    }
                    point.volume?.let { aggregatedVolume += it }
                }
            }

            result.reversed()
        }
    }

    fun exchangesSingle(limit: Int, page: Int): Single<List<Exchange>> {
        return coinGeckoService.exchanges(limit, page)
            .map { list ->
                list.map { Exchange(it.id, it.name, it.image) }
            }
    }

    fun historicalPriceSingle(
        id: String,
        currencyCode: String,
        timestamp: Long
    ): Single<BigDecimal> {

        val tsDiff = ((System.currentTimeMillis() / 1000) - timestamp) / 3600 //Diff in hours
        val startTime =
            if (tsDiff < 24) timestamp - MINUTES_10_IN_SECONDS else timestamp - HOURS_2_IN_SECONDS
        val endTime =
            if (tsDiff < 24) timestamp + MINUTES_10_IN_SECONDS else timestamp + HOURS_2_IN_SECONDS

        return coinGeckoService.coinMarketChartRange(
            id,
            currencyCode,
            startTime,
            endTime,
        ).map { result ->
            // result.price is List<BigDecimal>, where
            // price.get(0) is timestamp in milliseconds
            // price.get(1) is coin price

            //find price with nearest timestamp from fetched results
            val price = result.prices.minByOrNull {
                val timestampMillis = it[0].toLong()

                (timestampMillis / 1000L - timestamp).absoluteValue
            }!!.get(1)

            price
        }
    }

    fun marketTickersSingle(coinGeckoId: String): Single<CoinGeckoCoinResponse> {
        return coinGeckoService.marketTickers(
            coinGeckoId,
            "true",
            "false",
            "false",
            "false",
            "false",
            "false",
        )
    }

    private fun coinGeckoPointCount(chartType: ChartType) = when (chartType) {
        ChartType.TODAY -> chartType.points
        ChartType.DAILY -> chartType.points
        else -> chartType.points * 2
    }

    interface CoinGeckoService {

        @GET("coins/{coinId}/market_chart")
        fun coinMarketChart(
            @Path("coinId") coinId: String,
            @Query("vs_currency") vs_currency: String,
            @Query("days") days: Int,
            @Query("interval") interval: String?,
        ): Single<Response.HistoricalMarketData>

        @GET("coins/{coinId}/market_chart/range")
        fun coinMarketChartRange(
            @Path("coinId") coinId: String,
            @Query("vs_currency") vs_currency: String,
            @Query("from") from: Long,
            @Query("to") to: Long,
        ): Single<Response.HistoricalMarketData>

        @GET("exchanges")
        fun exchanges(
            @Query("per_page") limit: Int,
            @Query("page") days: Int,
        ): Single<List<ExchangeRaw>>

        @GET("coins/{coinId}")
        fun marketTickers(
            @Path("coinId") coinId: String,
            @Query("tickers") tickers: String,
            @Query("localization") localization: String,
            @Query("market_data") marketData: String,
            @Query("community_data") communityData: String,
            @Query("developer_data") developerData: String,
            @Query("sparkline") sparkline: String,
        ): Single<CoinGeckoCoinResponse>


        object Response {
            data class HistoricalMarketData(
                val prices: List<List<BigDecimal>>,
                val market_caps: List<List<BigDecimal>>,
                val total_volumes: List<List<BigDecimal>>,
            )
        }
    }

    companion object{
        private const val MINUTES_10_IN_SECONDS = 60 * 10
        private const val HOURS_2_IN_SECONDS = 60 * 60 * 2
    }

}
