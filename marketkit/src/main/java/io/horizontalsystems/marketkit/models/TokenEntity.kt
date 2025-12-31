package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    primaryKeys = ["coinUid", "blockchainUid", "type", "reference"],
    foreignKeys = [
        ForeignKey(
            entity = Coin::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("coinUid"),
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = BlockchainEntity::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("blockchainUid"),
            onDelete = CASCADE
        ),
    ],
    indices = [
        Index(value = arrayOf("coinUid")),
        Index(value = arrayOf("blockchainUid")),
        Index(value = arrayOf("blockchainUid", "type", "reference"))
    ]
)
data class TokenEntity(
    @SerializedName("coin_uid")
    val coinUid: String,
    @SerializedName("blockchain_uid")
    val blockchainUid: String,
    val type: String,
    val decimals: Int?,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val reference: String
) : Parcelable
