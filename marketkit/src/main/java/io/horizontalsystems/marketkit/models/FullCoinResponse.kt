package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName

data class FullCoinResponse(
    val uid: String,
    val name: String,
    val code: String,
    @SerializedName("market_cap_rank")
    val marketCapRank: Int?,
    @SerializedName("coingecko_id")
    val coinGeckoId: String,
    val platforms: List<PlatformResponse>,
) {

    fun fullCoin(): FullCoin {
        val coin = Coin(uid, name, code.uppercase(), marketCapRank, coinGeckoId)
        return FullCoin(coin, platforms.mapNotNull { it.platform(uid) })
    }

}
