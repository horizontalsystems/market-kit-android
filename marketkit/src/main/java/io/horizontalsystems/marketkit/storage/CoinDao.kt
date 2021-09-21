package io.horizontalsystems.marketkit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.MarketCoin
import io.horizontalsystems.marketkit.models.Platform

@Dao
interface CoinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(coin: Coin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(platform: Platform)

    @Query(
        "SELECT * FROM Coin " +
                "WHERE name LIKE :filter OR code LIKE :filter " +
                "ORDER BY marketCapRank DESC " +
                "LIMIT :limit"
    )
    fun getMarketCoins(filter: String, limit: Int): List<MarketCoin>

    @Query("SELECT * FROM Coin WHERE uid IN (:coinUids)")
    fun getMarketCoins(coinUids: List<String>): List<MarketCoin>


}
