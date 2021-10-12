package io.horizontalsystems.marketkit.models

import java.math.BigDecimal

data class ChartPoint(val value: BigDecimal, val volume: BigDecimal?, val timestamp: Long)
