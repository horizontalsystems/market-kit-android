package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CoinPriceResponse(
    val price: BigDecimal,
    @SerializedName("price_change_24h")
    val priceChange: BigDecimal,
    @SerializedName("last_updated")
    val lastUpdated: Long
) {
    fun coinPrice(coinUid: String, currencyCode: String): CoinPrice {
        return CoinPrice(coinUid, currencyCode, price, priceChange, lastUpdated)
    }
}
