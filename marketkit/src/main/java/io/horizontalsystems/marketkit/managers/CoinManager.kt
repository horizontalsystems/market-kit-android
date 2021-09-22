package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.reactivex.subjects.PublishSubject

class CoinManager(
    private val storage: CoinStorage
) {
    val marketCoinsUpdatedObservable = PublishSubject.create<Unit>()

    fun marketCoins(filter: String, limit: Int): List<MarketCoin> {
        return storage.marketCoins(filter, limit)
    }

    fun marketCoins(coinUids: List<String>): List<MarketCoin> {
        return storage.marketCoins(coinUids)
    }

    fun marketCoinsByCoinTypes(coinTypes: List<CoinType>): List<MarketCoin> {
        val platformCoins = storage.platformCoins(coinTypes)

        return storage.marketCoins(platformCoins.map { it.coin.uid })
    }

    fun platformCoin(coinType: CoinType): PlatformCoin? {
        return storage.platformCoin(coinType)
    }

    fun platformCoins(): List<PlatformCoin> {
        return storage.platformCoins()
    }

    fun platformCoins(coinTypes: List<CoinType>): List<PlatformCoin> {
        return storage.platformCoins(coinTypes)
    }

    fun save(coin: Coin, platform: Platform) {
        storage.save(coin, platform)
        marketCoinsUpdatedObservable.onNext(Unit)
    }

    fun coins(filter: String, limit: Int): List<Coin> {
        return storage.coins(filter, limit)
    }

    fun handleFetched(marketCoins: List<MarketCoin>) {
        storage.save(marketCoins)
        marketCoinsUpdatedObservable.onNext(Unit)
    }

}
