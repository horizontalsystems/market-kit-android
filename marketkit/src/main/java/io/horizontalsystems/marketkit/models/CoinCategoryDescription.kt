package io.horizontalsystems.marketkit.models

data class CoinCategoryDescription(
    val language: String,
    val content: String
) {
    override fun toString(): String {
        return "CoinCategoryDescription [language: $language; contentLength: ${content.length}]"
    }
}
