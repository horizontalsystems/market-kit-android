package io.horizontalsystems.marketkit.models

data class PlatformResponse(
    val type: String,
    val decimal: Int,
    val address: String?,
    val symbol: String?
)
