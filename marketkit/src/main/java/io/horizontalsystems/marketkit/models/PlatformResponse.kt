package io.horizontalsystems.marketkit.models

data class PlatformResponse(
    val type: String,
    val decimals: Int,
    val address: String?,
    val symbol: String?
)
