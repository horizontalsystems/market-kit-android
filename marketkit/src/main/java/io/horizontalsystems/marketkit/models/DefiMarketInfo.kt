package io.horizontalsystems.marketkit.models

import java.math.BigDecimal

data class DefiMarketInfo(
    val fullCoin: FullCoin?,
    val name: String,
    val logoUrl: String,
    val tvl: BigDecimal,
    val tvlRank: Int,
    val tvlChange1D: BigDecimal?,
    val tvlChange7D: BigDecimal?,
    val tvlChange30D: BigDecimal?,
    val chains: List<String>,
    val chainTvls: Map<String, BigDecimal?>,
) {
    constructor(defiMarketInfoResponse: DefiMarketInfoResponse, fullCoin: FullCoin?) : this(
        fullCoin,
        defiMarketInfoResponse.name,
        defiMarketInfoResponse.logoUrl,
        defiMarketInfoResponse.tvl,
        defiMarketInfoResponse.tvlRank,
        defiMarketInfoResponse.tvlChange1D,
        defiMarketInfoResponse.tvlChange7D,
        defiMarketInfoResponse.tvlChange30D,
        defiMarketInfoResponse.chains,
        defiMarketInfoResponse.chainTvls,
    )
}
