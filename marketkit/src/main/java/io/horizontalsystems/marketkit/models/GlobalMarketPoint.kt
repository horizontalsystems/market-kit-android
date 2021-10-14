package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class GlobalMarketPoint(
    val timestamp: Long,
    @SerializedName("market_cap")
    val marketCap: BigDecimal,
    val volume24h: BigDecimal,
    @SerializedName("market_cap_defi")
    val marketCapDefi: BigDecimal,
    val tvl: BigDecimal,
    @SerializedName("dominance_btc")
    val dominanceBtc: BigDecimal
)
