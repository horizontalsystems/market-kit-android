package io.horizontalsystems.marketkit.models

enum class TimePeriod(val v: String) {
    All("All"),
    Hour1("1h"),
    DayStart("DayStart"),
    Hour24("24h"),
    Day7("7d"),
    Day14("14d"),
    Day30("30d"),
    Day200("200d"),
    Year1("1y"),;

    companion object {
        fun fromString(v: String) = values().find { it.v == v }
    }
}
