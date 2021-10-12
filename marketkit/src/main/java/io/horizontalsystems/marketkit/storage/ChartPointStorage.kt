package io.horizontalsystems.marketkit.storage

import io.horizontalsystems.marketkit.models.ChartPointEntity
import io.horizontalsystems.marketkit.models.ChartType

class ChartPointStorage(marketDatabase: MarketDatabase) {
    private val chartPointDao = marketDatabase.chartPointDao()

    fun save(chartPoints: List<ChartPointEntity>) {
        chartPointDao.insert(chartPoints)
    }

    fun getList(
        coinUid: String,
        currencyCode: String,
        chartType: ChartType
    ): List<ChartPointEntity> {
        return chartPointDao.getList(coinUid, currencyCode, chartType)
    }

    fun delete(
        coinUid: String,
        currencyCode: String,
        chartType: ChartType
    ) {
        chartPointDao.delete(coinUid, currencyCode, chartType)
    }
}
