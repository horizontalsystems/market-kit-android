package io.horizontalsystems.marketkit.storage

import androidx.sqlite.db.SimpleSQLiteQuery
import io.horizontalsystems.marketkit.models.*

class CoinStorage(marketDatabase: MarketDatabase) {

    private val coinDao = marketDatabase.coinDao()

    fun coin(coinUid: String): Coin? =
        coinDao.getCoin(coinUid)

    fun coins(coinUids: List<String>): List<Coin> =
        coinDao.getCoins(coinUids)

    fun fullCoins(filter: String, limit: Int): List<FullCoin> {
        val sql = """
            SELECT * FROM Coin
            WHERE name LIKE '%$filter%' OR code LIKE '%$filter%'
            ORDER BY
                CASE
                    WHEN `coin`.`code` LIKE '$filter' THEN 1
                    WHEN `coin`.`code` LIKE '$filter%' THEN 2
                    WHEN `coin`.`name` LIKE '$filter%' THEN 3
                    ELSE 4 
                END,
                CASE 
                    WHEN `coin`.`marketCapRank` IS NULL THEN 1
                    ELSE 0 
                END,
                `coin`.`marketCapRank` ASC,
                `coin`.`name` ASC
            LIMIT $limit
        """.trimIndent()

        return coinDao.getFullCoins(SimpleSQLiteQuery(sql)).map { it.fullCoin }
    }

    fun fullCoin(uid: String): FullCoin? =
        coinDao.getFullCoin(uid)?.fullCoin

    fun fullCoins(uids: List<String>): List<FullCoin> =
        coinDao.getFullCoins(uids).map { it.fullCoin }

    fun getToken(query: TokenQuery): Token? {
        val sql = "SELECT * FROM TokenEntity WHERE ${filterByTokenQuery(query)} LIMIT 1"

        return coinDao.getToken(SimpleSQLiteQuery(sql))?.token
    }

    fun getTokens(queries: List<TokenQuery>): List<Token> {
        if (queries.isEmpty()) return listOf()

        val queriesStr = queries.toSet().toList().map { filterByTokenQuery(it) }.joinToString(" OR ")
        val sql = "SELECT * FROM TokenEntity WHERE $queriesStr"

        return coinDao.getTokens(SimpleSQLiteQuery(sql)).map { it.token }
    }

    fun getTokens(reference: String): List<Token> {
        val queriesStr = "`TokenEntity`.`reference` LIKE '%$reference'"
        val sql = "SELECT * FROM TokenEntity WHERE $queriesStr"

        return coinDao.getTokens(SimpleSQLiteQuery(sql)).map { it.token }
    }

    fun getTokens(blockchainType: BlockchainType, filter: String, limit: Int): List<Token> {
        val sql = """
            SELECT * FROM TokenEntity
            WHERE 
              `TokenEntity`.`blockchainUid` = '${blockchainType.uid}'
              AND coinUid IN (SELECT uid FROM Coin WHERE name LIKE '%$filter%' OR code LIKE '%$filter%')
            LIMIT $limit
        """.trimIndent()

        return coinDao.getTokens(SimpleSQLiteQuery(sql)).map { it.token }
    }

    fun getBlockchain(uid: String): Blockchain? =
        coinDao.getBlockchain(uid)?.blockchain

    fun getBlockchains(uids: List<String>): List<Blockchain> =
        coinDao.getBlockchains(uids).map { it.blockchain }

    private fun filterByTokenQuery(query: TokenQuery): String {
        val (type, reference) = query.tokenType.values

        val conditions = mutableListOf(
            "`TokenEntity`.`blockchainUid` = '${query.blockchainType.uid}'",
            "`TokenEntity`.`type` = '$type'"
        )

        if (reference != null) {
            conditions.add("`TokenEntity`.`reference` LIKE '%$reference'")
        }

        return conditions.joinToString(" AND ", "(", ")")
    }

    fun update(coins: List<Coin>, blockchainEntities: List<BlockchainEntity>, tokenEntities: List<TokenEntity>) {
        coinDao.deleteAllCoins()
        coinDao.deleteAllBlockchains()
        coinDao.deleteAllTokens()
        coins.forEach { coinDao.insert(it) }
        blockchainEntities.forEach { coinDao.insert(it) }
        tokenEntities.forEach { coinDao.insert(it) }
    }

}
