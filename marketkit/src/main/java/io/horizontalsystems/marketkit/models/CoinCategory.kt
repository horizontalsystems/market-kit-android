package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class CoinCategory(
    @PrimaryKey
    val uid: String,
    val name: String,
    val description: Map<String, String>,
    val order: Int
) : Parcelable {
    override fun toString(): String {
        return "CoinCategory [uid: $uid; name: $name; descriptionCount: ${description.size}; order: $order]"
    }
}
