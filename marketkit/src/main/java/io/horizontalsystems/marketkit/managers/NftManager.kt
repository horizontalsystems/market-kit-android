package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.HsNftApiV1Response
import io.horizontalsystems.marketkit.providers.HsNftProvider
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class NftManager(
    private val coinManager: CoinManager,
    private val provider: HsNftProvider
) {
    private val zeroAddress = "0x0000000000000000000000000000000000000000"

    fun collectionsFromResponses(responses: List<HsNftApiV1Response.Collection>): List<NftCollection> {
        val ethereumPlatformCoin = coinManager.token(TokenQuery(BlockchainType.Ethereum, TokenType.Native))

        return responses.map { response ->
            collectionFromResponse(response, ethereumPlatformCoin)
        }
    }

    suspend fun assetCollection(address: String): NftAssetCollection {
        val collections = collectionsFromResponses(provider.allCollections(address))
        val assets = assetsFromResponses(provider.allAssets(address))

        return NftAssetCollection(collections, assets)
    }

    suspend fun collection(uid: String): NftCollection =
        collectionFromResponse(provider.collection(uid))

    suspend fun collections(): List<NftCollection> =
        collectionsFromResponses(provider.allCollections())

    suspend fun asset(contractAddress: String, tokenId: String): NftAsset =
        assetFromResponse(provider.asset(contractAddress, tokenId))

    suspend fun assets(collectionUid: String, cursor: String? = null): PagedNftAssets {
        val assetResponses = provider.collectionAssets(collectionUid, cursor)

        return PagedNftAssets(
            assetsFromResponses(assetResponses.assets),
            assetResponses.cursor.next
        )
    }

    suspend fun eventsSingle(collectionUid: String, eventType: NftEvent.EventType?, tokenId: String?, cursor: String? = null): PagedNftEvents {
        val eventResponses = provider.collectionEvents(collectionUid, eventType, tokenId, cursor)

        return PagedNftEvents(
            eventsFromResponses(eventResponses.events),
            eventResponses.cursor.next
        )
    }

    private fun eventsFromResponses(events: List<HsNftApiV1Response.Event>): List<NftEvent> {
        val addresses: MutableList<String> = mutableListOf()

        for (event in events) {
            if (event.markets_data.payment_token != null) {
                addresses.add(event.markets_data.payment_token.address)
            }
        }

        val tokenMap = tokenMapFromAddresses(addresses)

        return events.mapNotNull { event ->
            var amount: NftPrice? = null

            val paymentToken = event.markets_data.payment_token
            if (paymentToken != null) {
                amount = nftPrice(tokenMap[paymentToken.address], event.amount, true)
            }

            return@mapNotNull NftEvent(
                assetFromResponse(event.asset),
                NftEvent.EventType.fromString(event.type),
                stringToDate(event.date),
                amount
            )
        }
    }

    private fun assetsFromResponses(assetResponses: List<HsNftApiV1Response.Asset>): List<NftAsset> {
        val addresses: MutableList<String> = mutableListOf()

        assetResponses.forEach { response ->
            if (response.markets_data.last_sale != null) {
                addresses.add(response.markets_data.last_sale.payment_token.address)
            }
        }

        val tokenMap = tokenMapFromAddresses(addresses)

        return assetResponses.map { response ->
            assetFromResponse(response, tokenMap)
        }
    }

    private fun assetFromResponse(response: HsNftApiV1Response.Asset, tokenMap: Map<String, Token>? = null): NftAsset {
        val tokenMapResolved: Map<String, Token> = if (tokenMap != null) {
            tokenMap
        } else {
            val addresses: MutableList<String> = mutableListOf()

            if (response.markets_data.last_sale != null) {
                addresses.add(response.markets_data.last_sale.payment_token.address)
            }

            response.markets_data.orders?.forEach { order ->
                addresses.add(order.payment_token_contract.address)
            }

            tokenMapFromAddresses(addresses)
        }

        return NftAsset(
            contract = NftCollection.Contract(response.contract.address, response.contract.type),
            collectionUid = response.collection_uid,
            tokenId = response.token_id,
            name = response.name,
            imageUrl = response.image_data?.image_url,
            imagePreviewUrl = response.image_data?.image_preview_url,
            description = response.description,
            externalLink = response.links?.external_link,
            permalink = response.links?.permalink,
            traits = response.attributes?.map { NftAsset.Trait(it.trait_type, it.value, it.trait_count) } ?: listOf(),
            lastSalePrice = response.markets_data.last_sale?.let { nftPrice(tokenMapResolved[it.payment_token.address], it.total_price, true) },
            onSale = response.markets_data.last_sale != null,
            orders = assetOrders(response.markets_data.orders, tokenMapResolved)
        )
    }

    private fun assetOrders(orders: List<HsNftApiV1Response.Asset.MarketsData.Order>?, tokenMap: Map<String, Token>): List<AssetOrder> =
        orders?.map { order ->
            val price = order.current_price.movePointLeft(order.payment_token_contract.decimals)
            AssetOrder(
                closingDate = stringToDate(order.closing_date),
                price = tokenMap[order.payment_token_contract.address]?.let { nftPrice(it, price, true) },
                emptyTaker = order.taker.address == zeroAddress,
                side = order.side,
                v = order.v,
                ethValue = price.multiply(order.payment_token_contract.eth_price)
            )
        } ?: listOf()

    private fun tokenMapFromAddresses(addresses: MutableList<String>): Map<String, Token> {
        try {
            if (addresses.isEmpty()) return mapOf()

            val map: MutableMap<String, Token> = mutableMapOf()

            val tokenTypes = addresses.map { getTokenType(it) }
            val tokens = coinManager.tokens(tokenTypes.map { TokenQuery(BlockchainType.Ethereum, it) })

            tokens.forEach { token ->
                when (token.type) {
                    TokenType.Native ->
                        map[zeroAddress] = token
                    is TokenType.Eip20 ->
                        map[token.type.address.lowercase()] = token

                    else -> {}
                }
            }

            return map
        } catch (e: Exception) {
            return mutableMapOf()
        }
    }

    private fun stringToDate(date: String) = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }
        sdf.parse(date)
    } catch (ex: Exception) {
        null
    }

    private fun getTokenType(paymentTokenAddress: String): TokenType {
        val coinType = when (paymentTokenAddress) {
            zeroAddress -> TokenType.Native
            else -> TokenType.Eip20(paymentTokenAddress.lowercase())
        }

        return coinType
    }

    private fun collectionFromResponse(
        response: HsNftApiV1Response.Collection,
        ethereumToken: Token? = null
    ): NftCollection {
        val ethereumToken = ethereumToken ?: coinManager.token(TokenQuery(BlockchainType.Ethereum, TokenType.Native))

        return NftCollection(
            asset_contracts = response.asset_contracts?.let { contracts ->
                contracts.map { contract ->
                    NftCollection.Contract(
                        address = contract.address,
                        schemaName = contract.type
                    )
                }
            } ?: listOf(),
            uid = response.uid,
            name = response.name,
            description = response.description,
            imageUrl = response.image_data?.image_url,
            featuredImageUrl = response.image_data?.featured_image_url,
            externalUrl = response.links?.external_url,
            discordUrl = response.links?.discord_url,
            twitterUsername = response.links?.twitter_username,
            stats = collectionStats(response.stats, ethereumToken),
            statCharts = statCharts(response.stats_chart, ethereumToken)
        )
    }

    private fun nftPrice(
        token: Token?,
        value: BigDecimal?,
        shift: Boolean
    ): NftPrice? {
        val token = token ?: return null
        val value = value ?: return null

        return NftPrice(
            token,
            if (shift) value.movePointLeft(token.decimals) else value
        )
    }

    private fun collectionStats(
        stats: HsNftApiV1Response.Collection.Stats,
        ethereumToken: Token?
    ): NftCollection.NftCollectionStats =
        NftCollection.NftCollectionStats(
            count = stats.count,
            ownersCount = stats.num_owners,
            totalSupply = stats.total_supply,
            averagePrice1d = nftPrice(ethereumToken, stats.one_day_average_price, false),
            averagePrice7d = nftPrice(ethereumToken, stats.seven_day_average_price, false),
            averagePrice30d = nftPrice(ethereumToken, stats.thirty_day_average_price, false),
            floorPrice = nftPrice(ethereumToken, stats.floor_price, false),
            totalVolume = stats.total_volume,
            marketCap = nftPrice(ethereumToken, stats.market_cap, false),
            volumes = mapOf(
                HsTimePeriod.Day1 to nftPrice(ethereumToken, stats.one_day_volume, false),
                HsTimePeriod.Week1 to nftPrice(ethereumToken, stats.seven_day_volume, false),
                HsTimePeriod.Month1 to nftPrice(
                    ethereumToken,
                    stats.thirty_day_volume,
                    false
                )
            ).filterValues { it != null } as Map<HsTimePeriod, NftPrice>,
            changes = mapOf(
                HsTimePeriod.Day1 to stats.one_day_change,
                HsTimePeriod.Week1 to stats.seven_day_change,
                HsTimePeriod.Month1 to stats.thirty_day_change
            ),
            sales = mapOf(
                HsTimePeriod.Day1 to stats.one_day_sales,
                HsTimePeriod.Week1 to stats.seven_day_sales,
                HsTimePeriod.Month1 to stats.thirty_day_sales
            )
        )

    private fun statCharts(
        statChartPoints: List<HsNftApiV1Response.Collection.ChartPoint>?,
        ethereumToken: Token?
    ): NftCollection.NftCollectionStatCharts? {
        val statChartPoints = statChartPoints ?: return null

        val oneDayVolumePoints = statChartPoints.mapNotNull { point ->
            point.one_day_volume?.let {
                NftCollection.NftCollectionStatCharts.PricePoint(
                    point.timestamp,
                    it,
                    ethereumToken
                )
            }
        }
        val averagePricePoints = statChartPoints.mapNotNull { point ->
            point.average_price?.let {
                NftCollection.NftCollectionStatCharts.PricePoint(
                    point.timestamp,
                    it,
                    ethereumToken
                )
            }
        }
        val floorPricePoints = statChartPoints.mapNotNull { point ->
            point.floor_price?.let {
                NftCollection.NftCollectionStatCharts.PricePoint(
                    point.timestamp,
                    it,
                    ethereumToken
                )
            }
        }
        val oneDaySalesPoints = statChartPoints.mapNotNull { point ->
            point.one_day_sales?.let {
                NftCollection.NftCollectionStatCharts.Point(
                    point.timestamp,
                    it
                )
            }
        }

        if (oneDayVolumePoints.isEmpty() && averagePricePoints.isEmpty() && floorPricePoints.isEmpty() && oneDayVolumePoints.isEmpty()) {
            return null
        }

        return NftCollection.NftCollectionStatCharts(
            oneDayVolumePoints = oneDayVolumePoints,
            averagePricePoints = averagePricePoints,
            floorPricePoints = floorPricePoints,
            oneDaySalesPoints = oneDaySalesPoints
        )
    }

}
