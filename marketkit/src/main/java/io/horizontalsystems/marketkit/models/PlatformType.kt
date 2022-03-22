package io.horizontalsystems.marketkit.models

enum class PlatformType {
    Ethereum,
    BinanceSmartChain,
    Polygon,
    Optimism,
    ArbitrumOne;

    val coinTypeIdPrefixes: List<String>
        get() = when (this) {
            Ethereum -> listOf("ethereum", "erc20")
            BinanceSmartChain -> listOf("binanceSmartChain", "bep20")
            Polygon -> listOf("polygon", "mrc20")
            Optimism -> listOf("ethereumOptimism", "optimismErc20")
            ArbitrumOne -> listOf("ethereumArbitrumOne", "arbitrumOneErc20")
        }
}
