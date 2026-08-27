package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.ProviderError
import io.horizontalsystems.marketkit.models.CoinHistoricalPrice
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinHistoricalPriceStorage
import java.math.BigDecimal
import kotlin.math.abs

class CoinHistoricalPriceManager(
    private val storage: CoinHistoricalPriceStorage,
    private val hsProvider: HsProvider,
) {

    suspend fun coinHistoricalPriceSingle(
        coinUid: String,
        currencyCode: String,
        timestamp: Long
    ): BigDecimal {

        storage.coinPrice(coinUid, currencyCode, timestamp)?.let {
            return it.value
        }

        val response = hsProvider.historicalCoinPriceSingle(coinUid, currencyCode, timestamp)
        if (abs(timestamp - response.timestamp) < 24 * 60 * 60) {
            val coinHistoricalPrice = CoinHistoricalPrice(coinUid, currencyCode, response.price, timestamp)
            storage.save(coinHistoricalPrice)
            return response.price
        } else {
            throw ProviderError.ReturnedTimestampIsVeryInaccurate()
        }
    }

    fun coinHistoricalPrice(coinUid: String, currencyCode: String, timestamp: Long): BigDecimal? {
        return storage.coinPrice(coinUid, currencyCode, timestamp)?.value
    }

}
