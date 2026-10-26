package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.MarketOverview
import io.horizontalsystems.marketkit.models.MarketOverviewResponse
import io.horizontalsystems.marketkit.providers.HsProvider

class MarketOverviewManager(
    private val hsProvider: HsProvider
) {

    private fun marketOverview(response: MarketOverviewResponse): MarketOverview =
        MarketOverview(
            globalMarketPoints = response.globalMarketPoints,
            coinCategories = response.coinCategories,
            topPlatforms = response.topPlatforms.map { it.topPlatform },
            topPairs = response.pairs
        )

    suspend fun marketOverviewSingle(currencyCode: String): MarketOverview =
        marketOverview(hsProvider.marketOverviewSingle(currencyCode))

}
