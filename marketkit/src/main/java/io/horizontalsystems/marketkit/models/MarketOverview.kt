package io.horizontalsystems.marketkit.models

data class MarketOverview(
    val globalMarketPoints: List<GlobalMarketPoint>,
    val coinCategories: List<CoinCategory>,
    val topPlatforms: List<TopPlatform>,
    val topPairs: List<TopPair>
)
