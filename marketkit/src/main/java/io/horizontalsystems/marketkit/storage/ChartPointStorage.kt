package io.horizontalsystems.marketkit.storage

import io.horizontalsystems.marketkit.models.ChartInfoKey
import io.horizontalsystems.marketkit.models.ChartPointEntity
import io.horizontalsystems.marketkit.models.HsTimePeriod

class ChartPointStorage(marketDatabase: MarketDatabase) {
    private val chartPointDao = marketDatabase.chartPointDao()

    fun save(chartPoints: List<ChartPointEntity>) {
        chartPointDao.insert(chartPoints)
    }

    fun getList(
        coinUid: String,
        currencyCode: String,
        interval: HsTimePeriod
    ): List<ChartPointEntity> {
        return chartPointDao.getList(coinUid, currencyCode, interval.value)
    }

    fun delete(key: ChartInfoKey) {
        chartPointDao.delete(key.coin.uid, key.currencyCode, key.interval.value)
    }
}
