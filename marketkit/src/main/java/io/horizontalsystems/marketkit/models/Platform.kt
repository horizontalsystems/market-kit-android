package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["coinType", "coinUid"])
data class Platform(
    val coinType: CoinType,
    val decimals: Int,
    val coinUid: String
): Parcelable {
    companion object {
        fun getInstance(platformResponse: PlatformResponse, coinUid: String): Platform? {
            val coinType =
                CoinType.getInstance(platformResponse.type, platformResponse.address, platformResponse.symbol)
                    ?: return null
            return Platform(coinType, platformResponse.decimals, coinUid)
        }
    }

    override fun toString(): String {
        return "Platform [coinType: $coinType; decimals: $decimals; coinUid: $coinUid]"
    }
}
