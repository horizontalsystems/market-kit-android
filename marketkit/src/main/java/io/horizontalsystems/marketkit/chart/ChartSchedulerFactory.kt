package io.horizontalsystems.marketkit.chart

import io.horizontalsystems.marketkit.models.ChartInfoKey
import io.horizontalsystems.marketkit.providers.CoinGeckoProvider
import io.horizontalsystems.marketkit.chart.scheduler.ChartScheduler

class ChartSchedulerFactory(
        private val manager: ChartManager,
        private val provider: CoinGeckoProvider) {

    private val retryInterval: Long = 30

    fun getScheduler(key: ChartInfoKey): ChartScheduler {
        return ChartScheduler(ChartSchedulerProvider(retryInterval, key, provider, manager))
    }
}
