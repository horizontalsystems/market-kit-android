package io.horizontalsystems.marketkit.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.horizontalsystems.marketkit.models.*


@Database(
    entities = [Coin::class, Platform::class, CoinCategory::class, CoinPrice::class, ChartPointEntity::class, GlobalMarketInfo::class, Exchange::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class MarketDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    abstract fun coinCategoryDao(): CoinCategoryDao
    abstract fun coinPriceDao(): CoinPriceDao
    abstract fun chartPointDao(): ChartPointDao
    abstract fun globalMarketInfoDao(): GlobalMarketInfoDao
    abstract fun exchangeDao(): ExchangeDao

    companion object {

        @Volatile
        private var INSTANCE: MarketDatabase? = null

        fun getInstance(context: Context): MarketDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): MarketDatabase {
            return Room.databaseBuilder(context, MarketDatabase::class.java, "marketKitDatabase")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
    }

}
