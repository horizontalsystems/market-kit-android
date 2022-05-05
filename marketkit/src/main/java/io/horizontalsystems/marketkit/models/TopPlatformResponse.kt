package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TopPlatformResponse(
    val uid: String,
    val name: String,
    val rank: Int,
    val protocols: Int,
    @SerializedName("market_cap")
    val marketCap: BigDecimal,
    val stats: Map<String, BigDecimal?>,
)
