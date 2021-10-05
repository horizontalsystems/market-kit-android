package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.*

data class MarketInfoOverviewRaw(
    val performance: Map<String, Map<String, BigDecimal?>>,
    @SerializedName("genesis_date")
    val genesisDate: Date?,
    @SerializedName("category_ids")
    val categoryIds: List<String>,
    val description: String,
    val links: Map<String, String>,
    @SerializedName("market_data")
    val marketData: MarketData,
) {
    data class MarketData(
        @SerializedName("market_cap")
        val marketCap: BigDecimal?,
        @SerializedName("market_cap_rank")
        val marketCapRank: Int?,
        @SerializedName("total_supply")
        val totalSupply: BigDecimal?,
        @SerializedName("circulating_supply")
        val circulatingSupply: BigDecimal?,
        @SerializedName("total_volume")
        val volume24h: BigDecimal?,
        @SerializedName("fully_diluted_valuation")
        val dilutedMarketCap: BigDecimal?,
        val tvl: BigDecimal?,
    )
}
