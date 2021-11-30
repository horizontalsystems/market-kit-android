package io.horizontalsystems.marketkit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CoinCategory(
    @PrimaryKey
    val uid: String,
    val name: String,
    val description: Map<String, String>,
    val order: Int
) {
    override fun toString(): String {
        return "CoinCategory [uid: $uid; name: $name; descriptionCount: ${description.size}; order: $order]"
    }
}
