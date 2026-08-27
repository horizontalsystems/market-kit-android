package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.managers.CoinPriceManager
import io.horizontalsystems.marketkit.managers.ICoinPriceCoinUidDataSource
import io.horizontalsystems.marketkit.models.CoinPrice

interface ISchedulerProvider {
    val id: String
    val lastSyncTimestamp: Long?
    val expirationInterval: Long

    suspend fun sync()
    fun notifyExpired()
}

class CoinPriceSchedulerProvider(
    private val currencyCode: String,
    private val manager: CoinPriceManager,
    private val provider: HsProvider
) : ISchedulerProvider {
    var dataSource: ICoinPriceCoinUidDataSource? = null

    override val id = "CoinPriceProvider"

    override val lastSyncTimestamp: Long?
        get() = manager.lastSyncTimeStamp(allCoinUids, currencyCode)

    override val expirationInterval: Long
        get() = CoinPrice.expirationInterval

    override suspend fun sync() {
        handle(provider.getCoinPrices(allCoinUids, currencyCode))
    }

    private val allCoinUids: List<String>
        get() = dataSource?.allCoinUids(currencyCode) ?: listOf()

    override fun notifyExpired() {
        manager.notifyExpired(allCoinUids, currencyCode)
    }

    private fun handle(updatedCoinPrices: List<CoinPrice>) {
        manager.handleUpdated(updatedCoinPrices, currencyCode)
    }
}
