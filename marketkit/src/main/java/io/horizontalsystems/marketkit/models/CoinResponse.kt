package io.horizontalsystems.marketkit.models

interface CoinResponse {
    val uid: String
    val name: String
    val code: String
    val marketCapRank: Int?
    val coinGeckoId: String

    fun coin() = Coin(uid, name, code, marketCapRank, coinGeckoId)
}
