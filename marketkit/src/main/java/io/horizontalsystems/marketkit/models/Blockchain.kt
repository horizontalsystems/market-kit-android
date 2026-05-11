package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.Objects

@Parcelize
@Serializable
data class Blockchain(
    val type: BlockchainType,
    val name: String,
    val eip3091url: String?
) : Parcelable {

    val uid: String
        get() = type.uid

    override fun equals(other: Any?): Boolean =
        other is Blockchain && other.type == type

    override fun hashCode(): Int =
        Objects.hash(type, name)

}
