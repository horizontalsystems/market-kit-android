package io.horizontalsystems.marketkit.chart

import io.horizontalsystems.marketkit.NoChartData
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.ChartPointStorage
import io.reactivex.Single
import java.util.*

class ChartManager(
    private val storage: ChartPointStorage,
    private val provider: HsProvider,
) {
    private fun chartInfo(points: List<ChartPoint>, periodType: HsPeriodType): ChartInfo? {
        val lastPoint = points.lastOrNull() ?: return null

        val lastPointTimestamp = lastPoint.timestamp
        val intervalRange = periodType.range
        val startTimestamp = intervalRange?.let {
            lastPointTimestamp - it
        } ?: points.first().timestamp

        val currentTimestamp = Date().time / 1000
        val lastPointGap = currentTimestamp - lastPointTimestamp

        // if points not in visible window (too early) just return null
        if (intervalRange != null && lastPointGap > intervalRange) {
            return null
        }

        return ChartInfo(
            points,
            startTimestamp,
            currentTimestamp,
            isExpired = lastPointGap > periodType.expiration
        )
    }

    private fun storedChartPoints(key: ChartInfoKey): List<ChartPoint> {
        return storage.getList(key.coinUid, key.currencyCode, key.periodType).map { point ->
            ChartPoint(
                point.value,
                point.timestamp,
                point.volume
            )
        }
    }

    fun update(points: List<ChartPoint>, key: ChartInfoKey) {
        val records = points.map { point ->
            ChartPointEntity(
                key.coinUid,
                key.currencyCode,
                key.periodType,
                point.value,
                point.volume,
                point.timestamp,
            )
        }

        storage.delete(key)
        storage.save(records)
    }

    fun getChartInfo(coinUid: String, currencyCode: String, periodType: HsPeriodType): ChartInfo? {
        val key = ChartInfoKey(coinUid, currencyCode, periodType)
        return chartInfo(storedChartPoints(key), periodType)
    }

    fun chartInfoSingle(
        coinUid: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): Single<ChartInfo> {
        return provider.coinPriceChartSingle(
            coinUid,
            currencyCode,
            periodType
        )
            .flatMap { response ->
                val points = response.map { it.chartPoint }

                chartInfo(points, periodType)?.let {
                    Single.just(it)
                } ?: Single.error(NoChartData())
            }
    }

    fun chartStartTimeSingle(coinUid: String): Single<Long> {
        return provider.coinPriceChartStartTime(coinUid)
    }
}
