package io.horizontalsystems.marketkit.models

import java.math.BigDecimal

data class MarketInfo(
    val fullCoin: FullCoin,
    val price: BigDecimal?,
    val priceChange1d: BigDecimal?,
    val priceChange24h: BigDecimal?,
    val priceChange7d: BigDecimal?,
    val priceChange14d: BigDecimal?,
    val priceChange30d: BigDecimal?,
    val priceChange90d: BigDecimal?,
    val priceChange200d: BigDecimal?,
    val priceChange1y: BigDecimal?,
    val priceChange2y: BigDecimal?,
    val priceChange3y: BigDecimal?,
    val priceChange4y: BigDecimal?,
    val priceChange5y: BigDecimal?,
    val marketCap: BigDecimal?,
    val marketCapRank: Int?,
    val totalVolume: BigDecimal?,
    val athPercentage: BigDecimal?,
    val atlPercentage: BigDecimal?,
    val listedOnTopExchanges: Boolean?,
    val solidCex: Boolean?,
    val solidDex: Boolean?,
    val goodDistribution: Boolean?,
    val advice: Analytics.TechnicalAdvice.Advice?,
    val categoryIds: List<Int>?
) {
    constructor(marketInfoRaw: MarketInfoRaw, fullCoin: FullCoin) : this(
        fullCoin,
        marketInfoRaw.price,
        marketInfoRaw.priceChange1d,
        marketInfoRaw.priceChange24h,
        marketInfoRaw.priceChange7d,
        marketInfoRaw.priceChange14d,
        marketInfoRaw.priceChange30d,
        marketInfoRaw.priceChange90d,
        marketInfoRaw.priceChange200d,
        marketInfoRaw.priceChange1y,
        marketInfoRaw.priceChange2y,
        marketInfoRaw.priceChange3y,
        marketInfoRaw.priceChange4y,
        marketInfoRaw.priceChange5y,
        marketInfoRaw.marketCap,
        marketInfoRaw.marketCapRank,
        marketInfoRaw.totalVolume,
        marketInfoRaw.athPercentage,
        marketInfoRaw.atlPercentage,
        marketInfoRaw.listedOnTopExchanges,
        marketInfoRaw.solidCex,
        marketInfoRaw.solidDex,
        marketInfoRaw.goodDistribution,
        marketInfoRaw.advice,
        marketInfoRaw.categoryIds
    )
}
