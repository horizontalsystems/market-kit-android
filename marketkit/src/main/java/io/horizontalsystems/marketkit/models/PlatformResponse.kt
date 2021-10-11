package io.horizontalsystems.marketkit.models

data class PlatformResponse(
    val type: String,
    val decimals: Int?,
    val address: String?,
    val symbol: String?
) {
    fun platform(coinUid: String): Platform? {
        if (decimals == null) return null
        val coinType = CoinType.getInstance(type, address, symbol) ?: return null

        return Platform(coinType, decimals, coinUid)
    }
}
