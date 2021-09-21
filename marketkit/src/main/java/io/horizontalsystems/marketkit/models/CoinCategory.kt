package io.horizontalsystems.marketkit.models

data class CoinCategory(
    val uid: String,
    val name: String,
    val descriptions: List<CoinCategoryDescription>
) {
    override fun toString(): String {
        return "CoinCategory [uid: $uid; name: $name; descriptionCount: ${descriptions.size}]"
    }
}
