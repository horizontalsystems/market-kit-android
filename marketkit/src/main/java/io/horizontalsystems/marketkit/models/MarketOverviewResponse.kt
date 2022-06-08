package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import io.horizontalsystems.marketkit.providers.HsNftApiV1Response

data class MarketOverviewResponse(
    @SerializedName("global")
    val globalMarketPoints: List<GlobalMarketPoint>,
    @SerializedName("sectors")
    val coinCategories: List<CoinCategory>,
    @SerializedName("platforms")
    val topPlatforms: List<TopPlatformResponse>,
    val nft: NftCollections
) {

    data class NftCollections(
        val one_day: List<HsNftApiV1Response.Collection>,
        val seven_day: List<HsNftApiV1Response.Collection>,
        val thirty_day: List<HsNftApiV1Response.Collection>
    )

}
