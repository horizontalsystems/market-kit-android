package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName

data class MarketOverviewResponse(
    @SerializedName("global")
    val globalMarketPoints: List<GlobalMarketPoint>,
    @SerializedName("sectors")
    val coinCategories: List<CoinCategory>,
    @SerializedName("platforms")
    val topPlatforms: List<TopPlatformResponse>,
) {

    val marketOverview: MarketOverview
        get() =
            MarketOverview(
                globalMarketPoints,
                coinCategories,
                topPlatforms.map { it.topPlatform }
            )

}
