package io.horizontalsystems.marketkit.chart

import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.ChartPointStorage
import io.reactivex.Single

class ChartManager(
    private val storage: ChartPointStorage,
    private val provider: HsProvider,
) {

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

    fun getChartInfo(
        coinUid: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): List<ChartPoint> {
        val key = ChartInfoKey(coinUid, currencyCode, periodType)
        return storedChartPoints(key)
    }

    fun chartInfoSingle(
        coinUid: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): Single<List<ChartPoint>> {
        return provider.coinPriceChartSingle(
            coinUid,
            currencyCode,
            periodType
        ).map { response ->
            response.map { it.chartPoint }
        }
    }

    fun chartStartTimeSingle(coinUid: String): Single<Long> {
        return provider.coinPriceChartStartTime(coinUid)
    }
}
