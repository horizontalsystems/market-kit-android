package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName

data class Vault(
    val address: String,
    val name: String,
    val apy: Apy,
    val tvl: String,
    val chain: String,
    val holders: Int?,
    val url: String?,

    @SerializedName("asset_symbol")
    val assetSymbol: String,

    @SerializedName("asset_logo")
    val assetLogo: String?,

    @SerializedName("protocol_name")
    val protocolName: String,

    @SerializedName("protocol_logo")
    val protocolLogo: String,

    @SerializedName("apy_chart")
    val apyChart: List<ApyChartPoint>
)

data class Apy(
    @SerializedName("1d")
    val oneDay: String,

    @SerializedName("7d")
    val sevenDay: String,

    @SerializedName("30d")
    val thirtyDay: String
)

data class ApyChartPoint(
    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("apy")
    val apy: String
)