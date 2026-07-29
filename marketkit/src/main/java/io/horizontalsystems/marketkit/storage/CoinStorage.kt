package io.horizontalsystems.marketkit.storage

import androidx.sqlite.db.SimpleSQLiteQuery
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainEntity
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.FullCoin
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenEntity
import io.horizontalsystems.marketkit.models.TokenQuery

class CoinStorage(val marketDatabase: MarketDatabase) {

    private val coinDao = marketDatabase.coinDao()
    private val sqliteMaxVariableNumber = 999
    private val sqliteMaxExpressionDepth = 500

    fun coin(coinUid: String): Coin? =
        coinDao.getCoin(coinUid)

    fun coins(coinUids: List<String>): List<Coin> =
        coinUids.chunked(sqliteMaxVariableNumber).flatMap { coinDao.getCoins(it) }

    fun allCoins(): List<Coin> = coinDao.getAllCoins()

    fun topFullCoins(limit: Int): List<FullCoin> {
        val sql = """
            SELECT * FROM Coin
            ORDER BY ${orderByMarketCapAndName()}
            LIMIT $limit
        """.trimIndent()

        return coinDao.getFullCoins(SimpleSQLiteQuery(sql)).map { it.fullCoin }
    }

    fun fullCoins(filter: String, limit: Int): List<FullCoin> {
        val (whereClause, whereArgs) = filterWhereStatement(filter)
        val (orderByClause, orderByArgs) = filterOrderByStatement(filter)
        val sql = """
            SELECT * FROM Coin
            WHERE $whereClause
            ORDER BY $orderByClause
            LIMIT $limit
        """.trimIndent()

        return coinDao.getFullCoins(SimpleSQLiteQuery(sql, (whereArgs + orderByArgs).toTypedArray()))
            .map { it.fullCoin }
    }

    fun fullCoin(uid: String): FullCoin? =
        coinDao.getFullCoin(uid)?.fullCoin

    fun fullCoins(uids: List<String>): List<FullCoin> =
        uids.chunked(sqliteMaxVariableNumber).flatMap { coinDao.getFullCoins(it).map { w -> w.fullCoin } }

    fun fullCoinsByCoinCodes(coinCodes: List<String>): List<FullCoin> =
        coinCodes.chunked(sqliteMaxVariableNumber).flatMap { coinDao.getFullCoinsByCoinCodes(it).map { w -> w.fullCoin } }

    fun getToken(query: TokenQuery): Token? {
        val (whereClause, whereArgs) = filterByTokenQuery(query)
        val sql = "SELECT * FROM TokenEntity WHERE $whereClause LIMIT 1"

        return coinDao.getToken(SimpleSQLiteQuery(sql, whereArgs.toTypedArray()))?.token
    }

    fun getTokens(queries: List<TokenQuery>): List<Token> {
        if (queries.isEmpty()) return listOf()

        // each query binds up to 3 variables, so respect both SQLite limits when chunking
        val chunkSize = minOf(sqliteMaxExpressionDepth, sqliteMaxVariableNumber / 3)
        return queries.distinct().chunked(chunkSize).flatMap { chunk ->
            val conditions = chunk.map { filterByTokenQuery(it) }
            val queriesStr = conditions.joinToString(" OR ") { it.first }
            val args = conditions.flatMap { it.second }
            val sql = "SELECT * FROM TokenEntity WHERE $queriesStr"
            coinDao.getTokens(SimpleSQLiteQuery(sql, args.toTypedArray())).map { it.token }
        }
    }

    fun getTokens(reference: String): List<Token> {
        val sql = "SELECT * FROM TokenEntity WHERE `TokenEntity`.`reference` LIKE ?"

        return coinDao.getTokens(SimpleSQLiteQuery(sql, arrayOf("%$reference"))).map { it.token }
    }

    fun getTokens(blockchainType: BlockchainType, filter: String, limit: Int): List<Token> {
        val (whereClause, whereArgs) = filterWhereStatement(filter)
        val (orderByClause, orderByArgs) = filterOrderByStatement(filter)
        val sql = """
            SELECT * FROM TokenEntity
            JOIN Coin ON `Coin`.`uid` = `TokenEntity`.`coinUid`
            WHERE
              `TokenEntity`.`blockchainUid` = ?
              AND ($whereClause)
            ORDER BY $orderByClause
            LIMIT $limit
        """.trimIndent()

        val args = (listOf(blockchainType.uid) + whereArgs + orderByArgs).toTypedArray()
        return coinDao.getTokens(SimpleSQLiteQuery(sql, args)).map { it.token }
    }

    fun getBlockchain(uid: String): Blockchain? =
        coinDao.getBlockchain(uid)?.blockchain

    fun getBlockchains(uids: List<String>): List<Blockchain> =
        uids.chunked(sqliteMaxVariableNumber).flatMap { coinDao.getBlockchains(it).map { e -> e.blockchain } }

    fun getAllBlockchains(): List<Blockchain> =
        coinDao.getAllBlockchains().map { it.blockchain }

    private fun filterByTokenQuery(query: TokenQuery): Pair<String, List<String>> {
        val (type, reference) = query.tokenType.values

        val conditions = mutableListOf(
            "`TokenEntity`.`blockchainUid` = ?",
            "`TokenEntity`.`type` = ?"
        )
        val args = mutableListOf(query.blockchainType.uid, type)

        if (reference.isNotBlank()) {
            conditions.add("`TokenEntity`.`reference` = ?")
            args.add(reference)
        }

        return Pair(conditions.joinToString(" AND ", "(", ")"), args)
    }

    private fun filterWhereStatement(filter: String): Pair<String, List<String>> {
        return if (filter.isBlank()) {
            Pair(
                "`Coin`.`code` IS NOT NULL AND `Coin`.`code` != '' AND `Coin`.`name` IS NOT NULL AND `Coin`.`name` != '' AND `Coin`.`marketCapRank` IS NOT NULL",
                listOf()
            )
        } else {
            Pair(
                "`Coin`.`name` LIKE ? OR `Coin`.`code` LIKE ?",
                listOf("%$filter%", "%$filter%")
            )
        }
    }

    private fun orderByMarketCapAndName() = """
        CASE 
            WHEN `Coin`.`marketCapRank` IS NULL THEN 1 
            ELSE 0 
        END, 
        `Coin`.`marketCapRank` ASC, 
        `Coin`.`name` ASC 
    """

    private fun filterOrderByStatement(filter: String): Pair<String, List<String>> {
        return if (filter.isBlank()) {
            Pair("`Coin`.`marketCapRank` ASC, `Coin`.`name` ASC", listOf())
        } else {
            Pair(
                """
        CASE
            WHEN `Coin`.`code` LIKE ? THEN 1
            WHEN `Coin`.`code` LIKE ? THEN 2
            WHEN `Coin`.`name` LIKE ? THEN 3
            ELSE 4
        END,
        ${orderByMarketCapAndName()}
        """,
                listOf(filter, "$filter%", "$filter%")
            )
        }
    }

    fun update(coins: List<Coin>, blockchainEntities: List<BlockchainEntity>, tokenEntities: List<TokenEntity>) {
        marketDatabase.runInTransaction {
            coinDao.deleteAllCoins()
            coinDao.deleteAllBlockchains()
            coinDao.deleteAllTokens()
            coins.forEach { coinDao.insert(it) }
            blockchainEntities.forEach { coinDao.insert(it) }
            tokenEntities.forEach { coinDao.insert(it) }
        }
    }

}
