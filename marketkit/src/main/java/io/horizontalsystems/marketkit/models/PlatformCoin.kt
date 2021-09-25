package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlatformCoin(
    @Embedded
    val platform: Platform,
    @Relation(
        entity = Coin::class,
        parentColumn = "coinUid",
        entityColumn = "uid"
    )
    val coin: Coin
): Parcelable {
    val marketCoin: MarketCoin
        get() = MarketCoin(coin, listOf(platform))

    val name: String
        get() = coin.name

    val code: String
        get() = coin.code

    val coinType: CoinType
        get() = platform.coinType

    val decimals: Int
        get() = platform.decimals

}
