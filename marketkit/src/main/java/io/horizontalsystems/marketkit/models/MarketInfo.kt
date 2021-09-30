package io.horizontalsystems.marketkit.models

import java.math.BigDecimal

data class MarketInfo(
    val coin: Coin,
    val price: BigDecimal,
    val priceChange: BigDecimal,
    val marketCap: BigDecimal,
    val totalVolume: BigDecimal,
) {
    constructor(marketInfoResponse: MarketInfoResponse) : this(
        Coin(marketInfoResponse),
        marketInfoResponse.price,
        marketInfoResponse.priceChange,
        marketInfoResponse.marketCap,
        marketInfoResponse.totalVolume,
    )

    enum class OrderField(val v: String) {
        PriceChange("price_change"),
        MarketCap("market_cap"),
        TotalVolume("total_volume")
    }

    enum class OrderDirection(val v: String) {
        Ascending("ASC"),
        Descending("DESC")
    }

    data class Order(val field: OrderField, val direction: OrderDirection)
}
