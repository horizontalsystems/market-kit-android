package io.horizontalsystems.marketkit.models

import java.math.BigDecimal
import java.util.Date

data class MarketInfoOverview(
    val fullCoin: FullCoin,
    val marketCap: BigDecimal?,
    val marketCapRank: Int?,
    val totalSupply: BigDecimal?,
    val circulatingSupply: BigDecimal?,
    val volume24h: BigDecimal?,
    val dilutedMarketCap: BigDecimal?,
    val tvl: BigDecimal?,
    val performance: Map<String, Map<HsTimePeriod, BigDecimal>>,
    val genesisDate: Date?,
    val categories: List<CoinCategory>,
    val description: String,
    val links: Map<LinkType, String>,
) {
    companion object {
        fun hsTimePeriodToStr(p: HsTimePeriod) = when (p) {
            HsTimePeriod.Day1 -> "1d"
            HsTimePeriod.Week1 -> "7d"
            HsTimePeriod.Week2 -> "14d"
            HsTimePeriod.Month1 -> "30d"
            HsTimePeriod.Month3 -> "90d"
            HsTimePeriod.Month6 -> "200d"
            HsTimePeriod.Year1 -> "1y"
            HsTimePeriod.Year2 -> "2y"
            HsTimePeriod.Year5 -> "5y"
        }

        fun strToHsTimePeriod(s: String) = when (s) {
            "1d" -> HsTimePeriod.Day1
            "7d" -> HsTimePeriod.Week1
            "14d" -> HsTimePeriod.Week2
            "30d" -> HsTimePeriod.Month1
            "90d" -> HsTimePeriod.Month3
            "200d" -> HsTimePeriod.Month6
            "1y" -> HsTimePeriod.Year1
            "2y" -> HsTimePeriod.Year2
            "5y" -> HsTimePeriod.Year5
            else -> null
        }
    }
}
