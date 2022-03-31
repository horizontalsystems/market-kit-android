package io.horizontalsystems.marketkit.storage

import androidx.sqlite.db.SimpleSQLiteQuery
import io.horizontalsystems.marketkit.models.*

class CoinStorage(marketDatabase: MarketDatabase) {

    private val coinDao = marketDatabase.coinDao()

    fun fullCoins(filter: String, limit: Int): List<FullCoin> {
        val sql = "SELECT * FROM Coin " +
                "WHERE name LIKE '%$filter%' OR code LIKE '%$filter%' " +
                "ORDER BY ${searchOrder(filter)} " +
                "LIMIT $limit"

        return coinDao.getMarketCoins(SimpleSQLiteQuery(sql))
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

    fun platformCoins(platformType: PlatformType, filter: String, limit: Int): List<PlatformCoin> {
        val platformCondition =
            "platform.coinType LIKE '${platformType.evmCoinTypeIdPrefix}%' OR platform.coinType = '${platformType.baseCoinType.id}%'"

        val query =
            """
                SELECT * FROM Platform 
                LEFT JOIN Coin AS coin 
                ON platform.coinUid == coin.uid 
                WHERE (coin.name LIKE '%$filter%' OR coin.code LIKE '%$filter%')
                AND ($platformCondition)
                ORDER BY ${searchOrder(filter)}
                LIMIT $limit
                """

        return coinDao.getPlatformCoins(SimpleSQLiteQuery(query))
    }

    private fun searchOrder(filter: String): String {
        val orderQuery = "" +
                "CASE " +
                "WHEN `coin`.`code` LIKE '$filter' THEN 1 " +
                "WHEN `coin`.`code` LIKE '$filter%' THEN 2 " +
                "WHEN `coin`.`name` LIKE '$filter%' THEN 3 " +
                "ELSE 4 END, " +

                "CASE WHEN `coin`.`marketCapRank` IS NULL THEN 1 ELSE 0 END, " +

                "`coin`.`marketCapRank` ASC, " +

                "`coin`.`name` ASC "

        return orderQuery
    }

    fun save(fullCoins: List<FullCoin>) {
        coinDao.save(fullCoins)
    }

    fun coin(coinUid: String): Coin? {
        return coinDao.coin(coinUid)
    }

}
