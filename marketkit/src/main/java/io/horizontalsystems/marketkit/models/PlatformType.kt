package io.horizontalsystems.marketkit.models

enum class PlatformType {
    Ethereum,
    BinanceSmartChain,
    Polygon,
    Optimism,
    ArbitrumOne;

    val baseCoinType: CoinType
        get() = when (this) {
            Ethereum -> CoinType.Ethereum
            BinanceSmartChain -> CoinType.BinanceSmartChain
            Polygon -> CoinType.Polygon
            Optimism -> CoinType.EthereumOptimism
            ArbitrumOne -> CoinType.EthereumArbitrumOne
        }

    val evmCoinTypeIdPrefix: String
        get() = when (this) {
            Ethereum -> "erc20"
            BinanceSmartChain -> "bep20"
            Polygon -> "mrc20"
            Optimism -> "optimismErc20"
            ArbitrumOne -> "arbitrumOneErc20"
        }
}
