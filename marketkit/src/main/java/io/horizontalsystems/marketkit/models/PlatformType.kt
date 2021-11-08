package io.horizontalsystems.marketkit.models

enum class PlatformType {
    Ethereum,
    BinanceSmartChain;

    val coinTypeIdPrefixes: List<String>
        get() = when (this) {
            Ethereum -> listOf("ethereum", "erc20")
            BinanceSmartChain -> listOf("binanceSmartChain", "bep20")
        }
}
