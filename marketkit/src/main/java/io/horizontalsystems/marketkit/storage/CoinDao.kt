package io.horizontalsystems.marketkit.storage

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import io.horizontalsystems.marketkit.models.*

@Dao
interface CoinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(coin: Coin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(platform: Platform)

    @Transaction
    @Query(
        "SELECT * FROM Coin " +
                "WHERE name LIKE :filter OR code LIKE :filter " +
                "ORDER BY CASE WHEN marketCapRank IS NULL THEN 1 ELSE 0 END, marketCapRank ASC " +
                "LIMIT :limit"
    )
    fun getMarketCoins(filter: String, limit: Int): List<FullCoin>

    @RawQuery
    fun getMarketCoins(query: SupportSQLiteQuery): List<FullCoin>

    @Transaction
    @Query("SELECT * FROM Platform WHERE coinType IN (:coinTypes)")
    fun getPlatformCoins(coinTypes: List<CoinType>): List<PlatformCoin>

    @Transaction
    @Query("SELECT * FROM Platform WHERE coinType IN (:coinTypeIds)")
    fun getPlatformCoinsByCoinTypeIds(coinTypeIds: List<String>): List<PlatformCoin>

    @Transaction
    @Query("SELECT * FROM Platform WHERE coinType = :coinType")
    fun getPlatformCoin(coinType: CoinType): PlatformCoin?

    @Transaction
    @RawQuery
    fun getPlatformCoins(query: SupportSQLiteQuery): List<PlatformCoin>

    @Transaction
    fun save(fullCoins: List<FullCoin>) {
        fullCoins.forEach { marketCoin ->
            insert(marketCoin.coin)
            marketCoin.platforms.forEach { platform ->
                insert(platform)
            }
        }
    }

    @Query("SELECT * FROM Coin WHERE uid IN (:coinUid)")
    fun coin(coinUid: String): Coin?

    @Query("SELECT * FROM Coin WHERE uid IN (:coinUids)")
    fun coins(coinUids: List<String>): List<Coin>

    @Query("DELETE FROM Coin")
    fun deleteAllCoins()

    @Query("DELETE FROM Platform")
    fun deleteAllPlatforms()

}
