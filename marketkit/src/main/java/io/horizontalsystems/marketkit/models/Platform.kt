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
    override fun toString(): String {
        return "Platform [coinType: $coinType; decimals: $decimals; coinUid: $coinUid]"
    }
}
