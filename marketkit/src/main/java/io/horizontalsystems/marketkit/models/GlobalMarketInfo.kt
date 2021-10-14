package io.horizontalsystems.marketkit.models

import androidx.room.Entity

@Entity(primaryKeys = ["currencyCode", "timePeriod"])
class GlobalMarketInfo(
    val currencyCode: String,
    val timePeriod: TimePeriod,
    val points: List<GlobalMarketPoint>,
    val timestamp: Long
)
