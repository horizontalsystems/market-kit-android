package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TopPlatformResponse(
    val name: String,
    @SerializedName("market_cap")
    val marketCap: BigDecimal,
    val rank: Int,
    val stats: Map<String, BigDecimal?>,
)
