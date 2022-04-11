package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CoinCategoryMarketData(
    val uid: String,
    @SerializedName("market_cap")
    val marketCap: BigDecimal?,
    @SerializedName("change_24h")
    val diff24H: BigDecimal?,
    @SerializedName("change_1w")
    val diff1W: BigDecimal?,
    @SerializedName("change_1m")
    val diff1M: BigDecimal?,
)
