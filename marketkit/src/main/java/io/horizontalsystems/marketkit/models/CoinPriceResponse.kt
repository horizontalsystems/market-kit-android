package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CoinPriceResponse(
    val price: BigDecimal,
    @SerializedName("price_change")
    val priceChange: BigDecimal,
    @SerializedName("last_updated")
    val lastUpdated: Long
)
