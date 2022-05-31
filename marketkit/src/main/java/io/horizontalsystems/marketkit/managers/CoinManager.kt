package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.CoinGeckoProvider
import io.horizontalsystems.marketkit.providers.DefiYieldProvider
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class CoinManager(
    private val storage: CoinStorage,
    private val hsProvider: HsProvider,
    private val coinGeckoProvider: CoinGeckoProvider,
    private val defiYieldProvider: DefiYieldProvider,
    private val exchangeManager: ExchangeManager
) {
    val fullCoinsUpdatedObservable = PublishSubject.create<Unit>()

    fun coin(uid: String): Coin? = storage.coin(uid)

    fun fullCoins(filter: String, limit: Int): List<FullCoin> {
        return storage.fullCoins(filter, limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        return storage.fullCoins(coinUids)
    }

    fun fullCoinsByCoinTypes(coinTypes: List<CoinType>): List<FullCoin> {
        val platformCoins = storage.platformCoins(coinTypes)

        return storage.fullCoins(platformCoins.map { it.coin.uid })
    }

    fun marketInfosSingle(top: Int, currencyCode: String, defi: Boolean): Single<List<MarketInfo>> {
        return hsProvider.marketInfosSingle(top, currencyCode, defi).map {
            getMarketInfos(it)
        }
    }

    fun advancedMarketInfosSingle(top: Int, currencyCode: String): Single<List<MarketInfo>> {
        return hsProvider.advancedMarketInfosSingle(top, currencyCode).map {
            getMarketInfos(it)
        }
    }

    fun marketInfosSingle(coinUids: List<String>, currencyCode: String): Single<List<MarketInfo>> {
        return hsProvider.marketInfosSingle(coinUids, currencyCode).map {
            getMarketInfos(it)
        }
    }

    fun marketInfosSingle(categoryUid: String, currencyCode: String): Single<List<MarketInfo>> {
        return hsProvider.marketInfosSingle(categoryUid, currencyCode).map {
            getMarketInfos(it)
        }
    }

    fun marketInfoOverviewSingle(
        coinUid: String,
        currencyCode: String,
        language: String
    ): Single<MarketInfoOverview> {
        return hsProvider.getMarketInfoOverview(coinUid, currencyCode, language)
    }

    fun marketTickersSingle(coinUid: String): Single<List<MarketTicker>> {
        val coinGeckoId = storage.coin(coinUid)?.coinGeckoId ?: return Single.just(emptyList())

        return coinGeckoProvider.marketTickersSingle(coinGeckoId)
            .map { response ->
                val coinUids =
                    (response.tickers.map { it.coinId } + response.tickers.mapNotNull { it.targetCoinId }).distinct()
                val coins = storage.coins(coinUids)
                val imageUrls = exchangeManager.imageUrlsMap(response.exchangeIds)
                response.marketTickers(imageUrls, coins)
            }
    }

    fun platformCoin(coinType: CoinType): PlatformCoin? {
        return storage.platformCoin(coinType)
    }

    fun platformCoins(platformType: PlatformType, filter: String, limit: Int): List<PlatformCoin> {
        return storage.platformCoins(platformType, filter, limit)
    }

    fun platformCoins(coinTypes: List<CoinType>): List<PlatformCoin> {
        return storage.platformCoins(coinTypes)
    }

    fun platformCoinsByCoinTypeIds(coinTypeIds: List<String>): List<PlatformCoin> {
        return storage.platformCoinsByCoinTypeIds(coinTypeIds)
    }

    fun handleFetched(fullCoins: List<FullCoin>) {
        storage.update(fullCoins)
        fullCoinsUpdatedObservable.onNext(Unit)
    }

    fun defiMarketInfosSingle(currencyCode: String): Single<List<DefiMarketInfo>> {
        return hsProvider.defiMarketInfosSingle(currencyCode).map {
            getDefiMarketInfos(it)
        }
    }

    fun marketInfoDetailsSingle(coinUid: String, currency: String): Single<MarketInfoDetails> {
        return hsProvider.getMarketInfoDetails(coinUid, currency).map {
            MarketInfoDetails(it)
        }
    }

    fun marketInfoTvlSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<ChartPoint>> {
        return hsProvider.marketInfoTvlSingle(coinUid, currencyCode, timePeriod)
    }

    fun marketInfoGlobalTvlSingle(
        chain: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<ChartPoint>> {
        return hsProvider.marketInfoGlobalTvlSingle(chain, currencyCode, timePeriod)
    }

    fun topHoldersSingle(coinUid: String): Single<List<TokenHolder>> {
        return hsProvider.topHoldersSingle(coinUid)
    }

    fun treasuriesSingle(coinUid: String, currencyCode: String): Single<List<CoinTreasury>> {
        return hsProvider.coinTreasuriesSingle(coinUid, currencyCode)
    }

    fun investmentsSingle(coinUid: String): Single<List<CoinInvestment>> {
        return hsProvider.investmentsSingle(coinUid)
    }

    fun coinReportsSingle(coinUid: String): Single<List<CoinReport>> {
        return hsProvider.coinReportsSingle(coinUid)
    }

    fun auditReportsSingle(addresses: List<String>): Single<List<Auditor>> {
        return defiYieldProvider.auditReportsSingle(addresses)
    }

    fun topPlatformsSingle(currencyCode: String): Single<List<TopPlatform>> {
        return hsProvider.topPlatformsSingle(currencyCode)
            .map { responseList ->
                responseList.map {
                    TopPlatform(
                        it.uid,
                        it.name,
                        it.rank,
                        it.protocols,
                        it.marketCap,
                        it.stats["rank_1d"]?.toInt(),
                        it.stats["rank_1w"]?.toInt(),
                        it.stats["rank_1m"]?.toInt(),
                        it.stats["change_1d"],
                        it.stats["change_1w"],
                        it.stats["change_1m"],
                    )
                }
            }
    }

    fun topPlatformsMarketCapPointsSingle(chain: String): Single<List<TopPlatformMarketCapPoint>> {
        return hsProvider.topPlatformMarketCapPointsSingle(chain)
    }

    private fun getMarketInfos(rawMarketInfos: List<MarketInfoRaw>): List<MarketInfo> {
        return try {
            val fullCoins = storage.fullCoins(rawMarketInfos.map { it.uid })
            val hashMap = fullCoins.map { it.coin.uid to it }.toMap()

            rawMarketInfos.mapNotNull { rawMarketInfo ->
                val fullCoin = hashMap[rawMarketInfo.uid] ?: return@mapNotNull null
                MarketInfo(rawMarketInfo, fullCoin)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getDefiMarketInfos(rawDefiMarketInfos: List<DefiMarketInfoResponse>): List<DefiMarketInfo> {
        val fullCoins = storage.fullCoins(rawDefiMarketInfos.mapNotNull { it.uid })
        val hashMap = fullCoins.map { it.coin.uid to it }.toMap()

        return rawDefiMarketInfos.map { rawDefiMarketInfo ->
            val fullCoin = hashMap[rawDefiMarketInfo.uid]
            DefiMarketInfo(rawDefiMarketInfo, fullCoin)
        }
    }

    fun dexLiquiditySingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod,
        sessionKey: String?
    ): Single<DexLiquiditiesResponse> {
        return hsProvider.dexLiquiditySingle(coinUid, currencyCode, timePeriod, sessionKey)
    }

    fun dexVolumesSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod,
        sessionKey: String?
    ): Single<DexVolumesResponse> {
        return hsProvider.dexVolumesSingle(coinUid, currencyCode, timePeriod, sessionKey)
    }

    fun transactionDataSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod,
        platform: String?,
        sessionKey: String?
    ): Single<TransactionsDataResponse> {
        return hsProvider.transactionDataSingle(
            coinUid,
            currencyCode,
            timePeriod,
            platform,
            sessionKey
        )
    }

    fun activeAddressesSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod,
        sessionKey: String?
    ): Single<ActiveAddressesDataResponse> {
        return hsProvider.activeAddressesSingle(coinUid, currencyCode, timePeriod, sessionKey)
    }

    fun topMoversSingle(currencyCode: String): Single<TopMovers> =
        hsProvider.topMoversRawSingle(currencyCode)
            .map { raw ->
                TopMovers(
                    gainers100 = getMarketInfos(raw.gainers100),
                    gainers200 = getMarketInfos(raw.gainers200),
                    gainers300 = getMarketInfos(raw.gainers300),
                    losers100 = getMarketInfos(raw.losers100),
                    losers200 = getMarketInfos(raw.losers200),
                    losers300 = getMarketInfos(raw.losers300)
                )
            }

}
