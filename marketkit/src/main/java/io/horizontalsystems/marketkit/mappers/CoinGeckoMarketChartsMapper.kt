package io.horizontalsystems.marketkit.mappers

import io.horizontalsystems.marketkit.models.ChartInfoKey
import io.horizontalsystems.marketkit.models.ChartPointEntity
import io.horizontalsystems.marketkit.providers.CoinGeckoProvider.CoinGeckoService.Response.HistoricalMarketData
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

class CoinGeckoMarketChartsMapper(private val intervalInSeconds: Long) {
    fun map(chartPointsResponse: HistoricalMarketData, chartPointKey: ChartInfoKey): List<ChartPointEntity> {
        val points = chartPointsResponse.prices.mapIndexed { index, priceValue ->
            val nullifyVolume = chartPointKey.chartType.resource != "histoday"

            val timestamp = priceValue[0].toLong() / 1000
            val price = priceValue[1]
            val volume = if (nullifyVolume) null else chartPointsResponse.total_volumes[index][1]

            ChartPointEntity(
                chartPointKey.coin.uid,
                chartPointKey.currencyCode,
                chartPointKey.chartType,
                price,
                volume,
                timestamp
            )
        }

        return normalize(points)
    }

    private fun nearest(timestamp: Long, truncInterval: Long): Long {
        val lower = floor(timestamp / truncInterval.toDouble()).toLong() * truncInterval

        return if (timestamp - lower > truncInterval / 2) (lower + truncInterval) else lower
    }

    private fun normalize(charts: List<ChartPointEntity>) : List<ChartPointEntity> {

        val normalized = mutableMapOf<Long, ChartPointEntity>()
        var latestDelta = 0L

        val normalizedInterval: Long
        val hourInterval: Long = 60 * 60
        val dayInterval = 24 * hourInterval

        // normalize to nearest minute if interval less than day
        // normalize to nearest hour if interval from day to
        // normalize to nearest day if other interval
        normalizedInterval = when {
            intervalInSeconds < hourInterval -> 60
            intervalInSeconds < dayInterval -> hourInterval
            else -> dayInterval
        }


        for (point in charts) {
            val timestamp = nearest(point.timestamp, normalizedInterval)
            val delta = abs(timestamp - point.timestamp)

            if (normalized[timestamp] != null && delta > latestDelta) {
                continue
            }

            normalized[timestamp] = point
            latestDelta = delta
        }

        return TreeMap(normalized).map { (timestamp, chartPoint) ->
            ChartPointEntity(chartPoint.coinUid, chartPoint.currencyCode, chartPoint.chartType, chartPoint.value, chartPoint.volume, timestamp)
        }
    }

}
