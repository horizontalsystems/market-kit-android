package io.horizontalsystems.marketkit.chart

import io.horizontalsystems.marketkit.managers.CoinManager
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.CoinGeckoProvider
import io.horizontalsystems.marketkit.storage.ChartPointStorage
import io.reactivex.Single
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class ChartManager(
    private val coinManager: CoinManager,
    private val storage: ChartPointStorage,
    private val provider: CoinGeckoProvider
) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(chartInfo: ChartInfo, key: ChartInfoKey)
        fun noChartInfo(key: ChartInfoKey)
    }

    private fun chartInfo(points: List<ChartPoint>, chartType: ChartType): ChartInfo? {
        val lastPoint = points.lastOrNull() ?: return null

        var endTimestamp = Date().time / 1000
        if (endTimestamp - chartType.rangeInterval > lastPoint.timestamp) {
            return null
        }

        val startTimestamp: Long
        if (chartType === ChartType.TODAY) {
            val zoneId = ZoneId.of("GMT")
            val localDate = LocalDate.now(zoneId).atStartOfDay(zoneId)

            val timestamp = Timestamp.from(localDate.toInstant())
            startTimestamp = timestamp.time / 1000

            val day = 24 * 60 * 60
            endTimestamp = startTimestamp + day
        } else {
            startTimestamp = lastPoint.timestamp - chartType.rangeInterval
        }

        val currentTimestamp = Date().time / 1000
        if (currentTimestamp - chartType.expirationInterval > lastPoint.timestamp) {
            return ChartInfo(
                points,
                startTimestamp,
                endTimestamp,
                isExpired = true
            )
        }

        return ChartInfo(
            points,
            startTimestamp,
            endTimestamp,
            isExpired = false
        )
    }

    private fun storedChartPoints(key: ChartInfoKey): List<ChartPoint> {
        return storage.getList(key.coin.uid, key.currencyCode, key.chartType).map {
            ChartPoint(it.value, it.volume, it.timestamp)
        }
    }

    fun update(points: List<ChartPointEntity>, key: ChartInfoKey) {

        storage.delete(key.coin.uid, key.currencyCode, key.chartType)
        storage.save(points)

        val chartInfo = chartInfo(points.map { ChartPoint(it.value, it.volume, it.timestamp) }, key.chartType)
        if (chartInfo == null) {
            listener?.noChartInfo(key)
        } else {
            listener?.onUpdate(chartInfo, key)
        }
    }

    fun handleNoChartPoints(key: ChartInfoKey) {
        listener?.noChartInfo(key)
    }

    fun getLastSyncTimestamp(key: ChartInfoKey): Long? {
        return storedChartPoints(key).lastOrNull()?.timestamp
    }

    fun getChartInfo(coinUid: String, currencyCode: String, chartType: ChartType): ChartInfo? {
        val fullCoin = coinManager.fullCoins(listOf(coinUid)).firstOrNull() ?: return null
        val key = ChartInfoKey(fullCoin.coin, currencyCode, chartType)
        return chartInfo(storedChartPoints(key), key.chartType)
    }

    fun chartInfoSingle(coinUid: String, currencyCode: String, chartType: ChartType): Single<ChartInfo> {
        val fullCoin = coinManager.fullCoins(listOf(coinUid)).firstOrNull()
            ?: return Single.error(Exception("No Chart Data"))

        val key = ChartInfoKey(fullCoin.coin, currencyCode, chartType)
        return provider.chartPointsSingle(key)
            .flatMap { points ->
                chartInfo(points.map { ChartPoint(it.value, it.volume, it.timestamp) }, chartType)?.let {
                    Single.just(it)
                } ?: Single.error(Exception("No Chart Data"))
            }
    }
}
