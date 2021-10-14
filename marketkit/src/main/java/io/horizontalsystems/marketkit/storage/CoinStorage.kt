package io.horizontalsystems.marketkit.storage

import androidx.sqlite.db.SimpleSQLiteQuery
import io.horizontalsystems.marketkit.models.*

class CoinStorage(marketDatabase: MarketDatabase) {

    private val coinDao = marketDatabase.coinDao()

    fun fullCoins(filter: String, limit: Int): List<FullCoin> {
        return coinDao.getMarketCoins("%$filter%", limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        val coinUidsList = coinUids.joinToString(", ") { "'$it'" }
        val sqlQuery = """
                      SELECT coin.*
                      FROM Coin as coin
                      LEFT JOIN Platform as platform ON coin.uid = platform.coinUid
                      WHERE coin.uid IN ($coinUidsList)
                      GROUP BY coin.uid
                      """
        return coinDao.getMarketCoins(SimpleSQLiteQuery(sqlQuery))
    }

    fun platformCoins(coinTypes: List<CoinType>): List<PlatformCoin> {
        return coinDao.getPlatformCoins(coinTypes)
    }

    fun platformCoinsByCoinTypeIds(coinTypeIds: List<String>): List<PlatformCoin> {
        return coinDao.getPlatformCoinsByCoinTypeIds(coinTypeIds)
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

    fun save(fullCoins: List<FullCoin>) {
        coinDao.save(fullCoins)
    }

    fun coin(coinUid: String): Coin? {
        return coinDao.coin(coinUid)
    }

}
