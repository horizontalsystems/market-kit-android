package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger

data class CoinResponse(
    val uid: String,
    val name: String,
    val code: String,
    @SerializedName("coingecko_id")
    val coinGeckoId: String,
    val price: String,
    @SerializedName("price_change_24h")
    val priceChange24h: BigDecimal,
    @SerializedName("market_cap")
    val marketCap: BigInteger,
    @SerializedName("market_cap_rank")
    val marketCapRank: Int?,
    @SerializedName("total_volume")
    val totalVolume: BigInteger,
    val platforms: List<PlatformResponse>,
)
