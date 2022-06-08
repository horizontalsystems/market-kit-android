package io.horizontalsystems.marketkit.models

import java.math.BigDecimal
import java.util.*

data class NftAsset(
    val contract: NftCollection.Contract,
    val collectionUid: String,
    val tokenId: String,
    val name: String?,
    val imageUrl: String?,
    val imagePreviewUrl: String?,
    val description: String?,
    val externalLink: String?,
    val permalink: String?,
    val traits: List<Trait>,
    val lastSalePrice: NftPrice?,
    val onSale: Boolean,
    val orders: List<AssetOrder>
) {

    data class Trait(
        val traitType: String,
        val value: String,
        val count: Int
    )

}

data class AssetOrder(
    val closingDate: Date?,
    val price: NftPrice?,
    val emptyTaker: Boolean,
    val side: Int,
    val v: Int?,
    val ethValue: BigDecimal
)

data class PagedNftAssets(
    val assets: List<NftAsset>,
    val cursor: String?
)
