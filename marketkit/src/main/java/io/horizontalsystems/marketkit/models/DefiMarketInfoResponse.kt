package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class DefiMarketInfoResponse(
    val uid: String?,
    val name: String,
    @SerializedName("logo")
    val logoUrl: String,
    val tvl: BigDecimal,
    @SerializedName("tvl_rank")
    val tvlRank: Int,
    @SerializedName("tvl_change_1d")
    val tvlChange1D: BigDecimal?,
    @SerializedName("tvl_change_7d")
    val tvlChange7D: BigDecimal?,
    @SerializedName("tvl_change_30d")
    val tvlChange30D: BigDecimal?,
    val chains: List<String>
)
