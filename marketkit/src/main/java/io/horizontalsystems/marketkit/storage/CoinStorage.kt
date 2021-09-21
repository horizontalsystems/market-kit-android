package io.horizontalsystems.marketkit.storage

import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.MarketCoin
import io.horizontalsystems.marketkit.models.Platform

class CoinStorage(marketDatabase: MarketDatabase) {

    private val coinDao = marketDatabase.coinDao()

    fun save(coin: Coin, platform: Platform) {
        coinDao.insert(coin)
        coinDao.insert(platform)
    }

    fun marketCoins(filter: String, limit: Int): List<MarketCoin> {
        return coinDao.getMarketCoins("%$filter%", limit)
    }

}
