package io.horizontalsystems.marketkit.models

data class CoinCategory(
    val uid: String,
    val name: String,
    val description: Map<String, String>,
) {

    override fun toString(): String {
        return "CoinCategory [uid: $uid; name: $name; descriptionCount: ${description.size}]"
    }

}
