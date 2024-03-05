package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.MarketOverview
import io.horizontalsystems.marketkit.models.MarketOverviewResponse
import io.horizontalsystems.marketkit.providers.HsProvider
import io.reactivex.Single

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

    fun marketOverviewSingle(currencyCode: String): Single<MarketOverview> =
        hsProvider.marketOverviewSingle(currencyCode).map { marketOverview(it) }

}
