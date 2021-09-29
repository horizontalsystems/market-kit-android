package io.horizontalsystems.marketkit.models

class MarketInfo(marketInfoResponse: MarketInfoResponse) {
    val coin = Coin(marketInfoResponse)
    val price = marketInfoResponse.price
    val priceChange = marketInfoResponse.priceChange
    val marketCap = marketInfoResponse.marketCap
    val totalVolume = marketInfoResponse.totalVolume

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
