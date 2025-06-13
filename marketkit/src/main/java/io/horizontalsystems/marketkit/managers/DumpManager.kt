package io.horizontalsystems.marketkit.managers

import android.database.DatabaseUtils
import io.horizontalsystems.marketkit.storage.MarketDatabase

class DumpManager(private val marketDatabase: MarketDatabase) {

    private val tablesCreation =
        "CREATE TABLE IF NOT EXISTS `BlockchainEntity` (`uid` TEXT NOT NULL, `name` TEXT NOT NULL, `eip3091url` TEXT, PRIMARY KEY(`uid`));\n" +
                "CREATE TABLE IF NOT EXISTS `Coin` (`uid` TEXT NOT NULL, `name` TEXT NOT NULL, `code` TEXT NOT NULL, `marketCapRank` INTEGER, `coinGeckoId` TEXT, `image` TEXT, PRIMARY KEY(`uid`));\n" +
                "CREATE TABLE IF NOT EXISTS `TokenEntity` (`coinUid` TEXT NOT NULL, `blockchainUid` TEXT NOT NULL, `type` TEXT NOT NULL, `decimals` INTEGER, `reference` TEXT NOT NULL, PRIMARY KEY(`coinUid`, `blockchainUid`, `type`, `reference`), FOREIGN KEY(`coinUid`) REFERENCES `Coin`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`blockchainUid`) REFERENCES `BlockchainEntity`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )\n"

    fun getInitialDump(): String {
        val esc: (String?) -> String = { it?.let(DatabaseUtils::sqlEscapeString) ?: "null" }
        val insertQueries = StringBuilder()
        insertQueries.append(tablesCreation)
        val blockchains = marketDatabase.blockchainEntityDao().getAll()
        blockchains.forEach { blockchain ->
            val insertQuery =
                "INSERT INTO BlockchainEntity VALUES(${esc(blockchain.uid)},${esc(blockchain.name)},${esc(blockchain.eip3091url)});"
            insertQueries.append(insertQuery).append("\n")
        }
        val tokens = marketDatabase.tokenEntityDao().getAll()
        tokens.forEach { token ->
            val insertQuery =
                "INSERT INTO TokenEntity VALUES(${esc(token.coinUid)},${esc(token.blockchainUid)},${esc(token.type)},${token.decimals},${esc(token.reference)});"
            insertQueries.append(insertQuery).append("\n")
        }
        val coins = marketDatabase.coinDao().getAllCoins()
        coins.forEach { coin ->
            val insertQuery =
                "INSERT INTO Coin VALUES(${esc(coin.uid)},${esc(coin.name)},${esc(coin.code)},${coin.marketCapRank},${esc(coin.coinGeckoId)},${esc(coin.image)});"
            insertQueries.append(insertQuery).append("\n")
        }
        return insertQueries.toString()
    }
}