package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Blockchain(
    val type: BlockchainType,
    val name: String
) : Parcelable {

    val uid: String
        get() = type.uid

}
