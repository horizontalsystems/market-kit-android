package io.horizontalsystems.marketkit.models

import androidx.room.Embedded
import androidx.room.Relation

data class FullCoin(
    @Embedded
    val coin: Coin,
    @Relation(
        entity = Platform::class,
        parentColumn = "uid",
        entityColumn = "coinUid"
    )
    val platforms: List<Platform>
) {
    constructor(coinResponse: FullCoinResponse) : this(
        Coin(coinResponse),
        coinResponse.platforms.mapNotNull { Platform.getInstance(it, coinResponse.uid) }
    )

    override fun toString(): String {
        return "MarketCoin [ \n$coin, \n${platforms.joinToString(separator = ",\n")} \n]"
    }
}
