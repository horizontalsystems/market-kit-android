package io.horizontalsystems.marketkit.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CoinCategory(
    val uid: String,
    val name: String,
    val description: Map<String, String>,
    val marketCap: BigDecimal?,
    @SerializedName("change_24h")
    val diff24H: BigDecimal?,
    @SerializedName("change_1w")
    val diff1W: BigDecimal?,
    @SerializedName("change_1m")
    val diff1M: BigDecimal?,
) {

    override fun toString(): String {
        return "CoinCategory [uid: $uid; name: $name; descriptionCount: ${description.size}]"
    }

}
