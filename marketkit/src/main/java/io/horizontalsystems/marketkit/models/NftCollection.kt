package io.horizontalsystems.marketkit.models

import java.math.BigDecimal

data class NftCollection(
    val asset_contracts: List<Contract>?,
    val uid: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val featuredImageUrl: String?,
    val externalUrl: String?,
    val discordUrl: String?,
    val twitterUsername: String?,

    val stats: NftCollectionStats,
    val statCharts: NftCollectionStatCharts?
) {

    data class Contract(
        val address: String,
        val schemaName: String,
    )

    data class NftCollectionStats(
        val count: Int?,
        val ownersCount: Int?,
        val totalSupply: Int,
        val averagePrice1d: NftPrice?,
        val averagePrice7d: NftPrice?,
        val averagePrice30d: NftPrice?,
        val floorPrice: NftPrice?,
        val totalVolume: BigDecimal?,
        val marketCap: NftPrice?,

        val volumes: Map<HsTimePeriod, NftPrice>,
        val changes: Map<HsTimePeriod, BigDecimal>,
        val sales: Map<HsTimePeriod, Int>,
    )

    data class NftCollectionStatCharts(
        val oneDayVolumePoints: List<PricePoint>,
        val averagePricePoints: List<PricePoint>,
        val floorPricePoints: List<PricePoint>,
        val oneDaySalesPoints: List<Point>
    ) {

        class PricePoint(
            timestamp: Long,
            value: BigDecimal,
            val token: Token?
        ) : Point(timestamp, value)

        open class Point(
            val timestamp: Long,
            val value: BigDecimal
        )

    }

}

data class NftPrice(
    val token: Token,
    val value: BigDecimal
)

data class NftAssetCollection(
    val collections: List<NftCollection>,
    val assets: List<NftAsset>
)
