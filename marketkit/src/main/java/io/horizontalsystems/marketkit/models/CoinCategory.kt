package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Parcelize
@Serializable
data class CoinCategory(
    val uid: String,
    val name: String,
    val description: Map<String, String>,
    @SerializedName("market_cap")
    @Serializable(with = BigDecimalSerializer::class)
    val marketCap: BigDecimal?,
    @SerializedName("change_24h")
    @Serializable(with = BigDecimalSerializer::class)
    val diff24H: BigDecimal?,
    @SerializedName("change_1w")
    @Serializable(with = BigDecimalSerializer::class)
    val diff1W: BigDecimal?,
    @SerializedName("change_1m")
    @Serializable(with = BigDecimalSerializer::class)
    val diff1M: BigDecimal?,
    @SerializedName("top_coins")
    val topCoins: List<String>,
) : Parcelable {

    override fun toString(): String {
        return "CoinCategory [uid: $uid; name: $name; descriptionCount: ${description.size}]"
    }

}

data class Category(
    val id: Int,
    val uid: String,
    val name: String,
    val order: Int,
    val description: Map<String, String>,
    @SerializedName("market_cap")
    val marketCap: String,
    @SerializedName("change_24h")
    val change24H: String,
    @SerializedName("change_1w")
    val change1W: String,
    @SerializedName("change_1m")
    val change1M: String
)
