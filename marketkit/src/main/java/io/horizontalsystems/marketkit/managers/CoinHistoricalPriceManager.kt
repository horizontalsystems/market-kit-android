package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.CoinNotFound
import io.horizontalsystems.marketkit.models.CoinHistoricalPrice
import io.horizontalsystems.marketkit.providers.CoinGeckoProvider
import io.horizontalsystems.marketkit.storage.CoinHistoricalPriceStorage
import io.reactivex.Single
import java.math.BigDecimal

class CoinHistoricalPriceManager(
    private val storage: CoinHistoricalPriceStorage,
    private val coinManager: CoinManager,
    private val coinGeckoProvider: CoinGeckoProvider
) {

    fun coinHistoricalPriceSingle(
        coinUid: String,
        currencyCode: String,
        timestamp: Long
    ): Single<BigDecimal> {

        storage.coinPrice(coinUid, currencyCode, timestamp)?.let {
            return Single.just(it.value)
        }

        val coinGeckoId =
            coinManager.coin(coinUid)?.coinGeckoId ?: return Single.error(CoinNotFound())

        return coinGeckoProvider.historicalPriceSingle(coinGeckoId, currencyCode, timestamp)
            .doOnSuccess {
                val coinHistoricalPrice = CoinHistoricalPrice(coinUid, currencyCode, it, timestamp)
                storage.save(coinHistoricalPrice)
            }
    }

    fun coinHistoricalPrice(coinUid: String, currencyCode: String, timestamp: Long): BigDecimal? {
        return storage.coinPrice(coinUid, currencyCode, timestamp)?.value
    }

}
