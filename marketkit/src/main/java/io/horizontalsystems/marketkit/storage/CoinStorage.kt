package io.horizontalsystems.marketkit.storage

import io.horizontalsystems.marketkit.models.*

class CoinStorage(marketDatabase: MarketDatabase) {

    private val coinDao = marketDatabase.coinDao()

    fun save(coin: Coin, platform: Platform) {
        coinDao.insert(coin)
        coinDao.insert(platform)
    }

    fun marketCoins(filter: String, limit: Int): List<MarketCoin> {
        return coinDao.getMarketCoins("%$filter%", limit)
    }

    fun marketCoins(coinUids: List<String>): List<MarketCoin> {
        return coinDao.getMarketCoins(coinUids)
    }

    fun platformCoins(coinTypes: List<CoinType>): List<PlatformCoin> {
        return coinDao.getPlatformCoins(coinTypes)
    }

    fun platformCoin(coinType: CoinType): PlatformCoin? {
        return coinDao.getPlatformCoin(coinType)
    }

    fun platformCoins(): List<PlatformCoin> {
        return coinDao.getPlatformCoins()
    }

    fun coins(filter: String, limit: Int): List<Coin> {
        return coinDao.getCoins("%$filter%", limit)
    }

    fun save(marketCoins: List<MarketCoin>) {
        coinDao.save(marketCoins)
    }

}
