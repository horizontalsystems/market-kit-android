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

    @SerializedName("1d")
    val oneDay: BigDecimal,

    @SerializedName("7d")
    val sevenDays: BigDecimal,

    @SerializedName("14d")
    val fourteenDays: BigDecimal,

    @SerializedName("30d")
    val thirtyDays: BigDecimal,

    @SerializedName("90d")
    val ninetyDays: BigDecimal,

    @SerializedName("200d")
    val twoHundredDays: BigDecimal,

    @SerializedName("1y")
    val oneYear: BigDecimal,

    @SerializedName("2y")
    val twoYears: BigDecimal,

    @SerializedName("3y")
    val threeYears: BigDecimal,

    @SerializedName("4y")
    val fourYears: BigDecimal,

    @SerializedName("5y")
    val fiveYears: BigDecimal,
)