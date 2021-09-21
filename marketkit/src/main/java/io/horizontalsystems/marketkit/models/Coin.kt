package io.horizontalsystems.marketkit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Coin(
    @PrimaryKey
    val uid: String,
    val name: String,
    val code: String,
    val marketCapRank: Int? = null,
    val coinGeckoId: String? = null
) {
    constructor(coinResponse: CoinResponse) : this(
        coinResponse.uid,
        coinResponse.name,
        coinResponse.code,
        coinResponse.marketCapRank,
        coinResponse.coinGeckoId
    )

    override fun equals(other: Any?): Boolean {
        return other is Coin && other.uid == uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }

    override fun toString(): String {
        return "Coin [uid: $uid; name: $name; code: $code; marketCapRank: $marketCapRank; coinGeckoId: $coinGeckoId]"
    }
}
