package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.GlobalMarketInfo
import io.horizontalsystems.marketkit.models.GlobalMarketPoint
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.GlobalMarketInfoStorage

class GlobalMarketInfoManager(
    private val provider: HsProvider,
    private val storage: GlobalMarketInfoStorage
) {
    private val expirationInterval = 600 // 10 minutes

    suspend fun globalMarketInfoSingle(currencyCode: String, timePeriod: HsTimePeriod): List<GlobalMarketPoint> {
        val currentTimestamp = System.currentTimeMillis() / 1000

        storage.globalMarketInfo(currencyCode, timePeriod)?.let { data ->
            if (currentTimestamp - data.timestamp <= expirationInterval)
                return data.points
        }

        val globalMarketPoints = provider.getGlobalMarketPointsSingle(currencyCode, timePeriod)
        storage.save(GlobalMarketInfo(currencyCode, timePeriod, globalMarketPoints, currentTimestamp))

        return globalMarketPoints
    }
}
