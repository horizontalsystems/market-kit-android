package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Stock(
    @SerializedName("uid")
    val uid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("market_price")
    val marketPrice: BigDecimal,

    @SerializedName("price_change")
    val priceChange: PriceChange
)

data class PriceChange(
    @SerializedName("1y")
    val oneYear: BigDecimal,

    @SerializedName("7d")
    val sevenDay: BigDecimal,

    @SerializedName("30d")
    val thirtyDay: BigDecimal,

    @SerializedName("90d")
    val ninetyDay: BigDecimal,

    @SerializedName("200d")
    val twoHundredDay: BigDecimal
)