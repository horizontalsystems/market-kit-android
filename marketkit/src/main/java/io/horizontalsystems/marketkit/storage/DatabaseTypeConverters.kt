package io.horizontalsystems.marketkit.storage

import androidx.room.TypeConverter
import io.horizontalsystems.marketkit.models.CoinType

class DatabaseTypeConverters {
    @TypeConverter
    fun fromCoinType(coinType: CoinType?): String {
        return coinType?.id ?: ""
    }

    @TypeConverter
    fun toCoinType(value: String): CoinType {
        return CoinType.fromString(value)
    }
}
