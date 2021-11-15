package io.horizontalsystems.marketkit.models

data class PlatformResponse(
    val type: String,
    val decimals: Int?,
    val address: String?,
    val symbol: String?
) {
    val coinType: CoinType?
        get() = CoinType.getInstance(type, address, symbol)

    fun platform(coinUid: String): Platform? {
        if (decimals == null) return null
        val coinType = coinType ?: return null

        return Platform(coinType, decimals, coinUid)
    }
}
