package io.horizontalsystems.marketkit.models

import androidx.room.Embedded
import androidx.room.Relation
import java.util.*

data class PlatformCoin(
    @Embedded
    val platform: Platform,
    @Relation(
        entity = Coin::class,
        parentColumn = "coinUid",
        entityColumn = "uid"
    )
    val coin: Coin
) {
    val marketCoin: MarketCoin
        get() = MarketCoin(coin, listOf(platform))

    val name: String
        get() = coin.name

    val code: String
        get() = coin.code

    val coinType: CoinType
        get() = platform.coinType

    val decimal: Int
        get() = platform.decimal

    override fun equals(other: Any?): Boolean {
        return other is PlatformCoin && other.platform == platform && other.coin == coin
    }

    override fun hashCode(): Int {
        return Objects.hash(platform, coin)
    }
}
