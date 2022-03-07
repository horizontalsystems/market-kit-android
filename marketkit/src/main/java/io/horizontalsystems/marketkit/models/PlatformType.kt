package io.horizontalsystems.marketkit.models

enum class PlatformType {
    Ethereum,
    BinanceSmartChain,
    Polygon;

    val coinTypeIdPrefixes: List<String>
        get() = when (this) {
            Ethereum -> listOf("ethereum", "erc20")
            BinanceSmartChain -> listOf("binanceSmartChain", "bep20")
            Polygon -> listOf("polygon", "mrc20")
        }
}
