package io.horizontalsystems.marketkit.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.Platform


@Database(entities = [Coin::class, Platform::class], version = 1, exportSchema = false)
@TypeConverters(DatabaseTypeConverters::class)
abstract class MarketDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
}
