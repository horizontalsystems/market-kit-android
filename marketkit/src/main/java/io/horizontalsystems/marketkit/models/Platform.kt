package io.horizontalsystems.marketkit.models

import androidx.room.Entity
import java.util.*

@Entity(primaryKeys = ["coinType", "coinUid"])
data class Platform(
    val coinType: CoinType,
    val decimal: Int,
    val coinUid: String
) {
    companion object {
        fun getInstance(platformResponse: PlatformResponse, coinUid: String): Platform? {
            val coinType = CoinType.getInstance(platformResponse.type, platformResponse.address, platformResponse.symbol) ?: return null
            return Platform(coinType, platformResponse.decimal, coinUid)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Platform && other.coinType == coinType && other.decimal == decimal && other.coinUid == coinUid
    }

    override fun hashCode(): Int {
        return Objects.hash(coinType, decimal, coinUid)
    }

    override fun toString(): String {
        return "Platform [coinType: $coinType; decimal: $decimal; coinUid: $coinUid]"
    }
}
