package io.horizontalsystems.marketkit.chart

import io.horizontalsystems.marketkit.models.HsPointTimePeriod
import io.horizontalsystems.marketkit.models.HsTimePeriod

object HsChartRequestHelper {

    fun pointInterval(interval: HsTimePeriod): HsPointTimePeriod {
        return when (interval) {
            HsTimePeriod.Day1 -> HsPointTimePeriod.Minute30
            HsTimePeriod.Week1 -> HsPointTimePeriod.Hour4
            HsTimePeriod.Week2 -> HsPointTimePeriod.Hour8
            else -> HsPointTimePeriod.Day1
        }
    }

    fun fromTimestamp(timestamp: Long, interval: HsTimePeriod, indicatorPoints: Int) : Long {
        // time needed for build indicators
        val pointInterval = pointInterval(interval)
        val additionalTime = indicatorPoints * pointInterval.interval

        return timestamp - interval.range - additionalTime
    }
}