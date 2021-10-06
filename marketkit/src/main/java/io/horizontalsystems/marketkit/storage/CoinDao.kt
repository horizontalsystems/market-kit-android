package io.horizontalsystems.marketkit.storage

import androidx.room.*
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

    @Transaction
    @Query("SELECT * FROM Coin WHERE uid IN (:coinUids)")
    fun getMarketCoins(coinUids: List<String>): List<FullCoin>

    @Transaction
    @Query("SELECT * FROM Platform WHERE coinType in (:coinTypes)")
    fun getPlatformCoins(coinTypes: List<CoinType>): List<PlatformCoin>

    @Transaction
    @Query("SELECT * FROM Platform WHERE coinType in (:coinTypeIds)")
    fun getPlatformCoinsByCoinTypeIds(coinTypeIds: List<String>): List<PlatformCoin>

    @Transaction
    @Query("SELECT * FROM Platform WHERE coinType = :coinType")
    fun getPlatformCoin(coinType: CoinType): PlatformCoin?

    @Transaction
    @Query("SELECT * FROM Platform")
    fun getPlatformCoins(): List<PlatformCoin>

    @Query(
        "SELECT * FROM Coin " +
                "WHERE name LIKE :filter OR code LIKE :filter " +
                "ORDER BY CASE WHEN marketCapRank IS NULL THEN 1 ELSE 0 END, marketCapRank ASC " +
                "LIMIT :limit"
    )
    fun getCoins(filter: String, limit: Int): List<Coin>

    @Transaction
    fun save(fullCoins: List<FullCoin>) {
        fullCoins.forEach { marketCoin ->
            insert(marketCoin.coin)
            marketCoin.platforms.forEach { platform ->
                insert(platform)
            }
        }
    }

}
