package io.horizontalsystems.marketkit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.marketkit.models.CoinCategory

@Dao
interface CoinCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(coinCategories: List<CoinCategory>)

    @Query("SELECT * FROM CoinCategory")
    fun getCoinCategories(): List<CoinCategory>

    @Query("SELECT * FROM CoinCategory WHERE uid IN(:uids)")
    fun getCoinCategories(uids: List<String>): List<CoinCategory>
}
