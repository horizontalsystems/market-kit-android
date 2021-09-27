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
                "ORDER BY marketCapRank DESC " +
                "LIMIT :limit"
    )
    fun getMarketCoins(filter: String, limit: Int): List<MarketCoin>

    @Transaction
    @Query("SELECT * FROM Coin WHERE uid IN (:coinUids)")
    fun getMarketCoins(coinUids: List<String>): List<MarketCoin>

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
                "ORDER BY marketCapRank ASC " +
                "LIMIT :limit"
    )
    fun getCoins(filter: String, limit: Int): List<Coin>

    @Transaction
    fun save(marketCoins: List<MarketCoin>) {
        marketCoins.forEach { marketCoin ->
            insert(marketCoin.coin)
            marketCoin.platforms.forEach { platform ->
                insert(platform)
            }
        }
    }

}
