package io.horizontalsystems.marketkit.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.models.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel(private val marketKit: MarketKit) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val authToken = ""

    fun runAudits() {
        val uniswapAddresses = listOf(
            "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984",
            "0xbf5140a22578168fd562dccf235e5d43a02ce9b1"
        )
        marketKit.auditReportsSingle(uniswapAddresses)
            .subscribeOn(Schedulers.io())
            .subscribe({ auditors ->
                auditors.forEach { auditor ->
                    Log.e("AAA", auditor.name)
                }
            }, {
                Log.e("AAA", "error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runInvestments() {
        val coinUid = "ethereum"

        marketKit.investmentsSingle(coinUid)
            .subscribeOn(Schedulers.io())
            .subscribe({ investments ->
                investments.forEach {
                    Log.e("AAA", it.round)
                }
            }, {
                Log.e("AAA", "error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runCoinReports() {
        val coinUid = "bitcoin"

        marketKit.coinReportsSingle(coinUid)
            .subscribeOn(Schedulers.io())
            .subscribe({ reports ->
                reports.forEach {
                    Log.e("AAA", it.body)
                }
            }, {
                Log.e("AAA", "error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runSyncCoins() {
        marketKit.sync()
        marketKit.refreshCoinPrices("USD")

        marketKit.coinPriceMapObservable(listOf("bitcoin", "ethereum", "solana"), "USD")
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "coinPrices: ${it.size}")
                it.forEach {
                    Log.w("AAA", "coinPrice ${it.key}: ${it.value}")
                }
            }, {
                Log.e("AAA", "coinPriceMapObservable error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runGetChartInfo() {
        val coinUid = "ethereum"
        val currencyCode = "USD"

        val time = Date().time / 1000 - TimeUnit.DAYS.toSeconds(7)

        val interval = HsPeriodType.ByStartTime(time)

        //fetch chartInfo from API
        marketKit.chartPointsSingle(coinUid, currencyCode, interval)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "fetchChartInfo: ${it}")
            }, {
                Log.e("AAA", "fetchChartInfo Error", it)
            })
            .let {
                disposables.add(it)
            }

        marketKit.chartStartTimeSingle(coinUid)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "chartStartTimeSingle: $it")
            }, {
                Log.e("AAA", "chartStartTimeSingle Error", it)

            })
            .let {
                disposables.add(it)
            }
    }

    fun runFilterFullCoins() {
        val filter = "if"
        val fullCoins = marketKit.fullCoins(filter, 100)
        Log.w("AAA", "Using filter $filter and got ${fullCoins.size} coins")
        fullCoins.forEach {
            Log.w("AAA", "Coin ${it.coin.code}, ${it.coin.name}, platforms: ${it.tokens}")
        }
    }

    fun runFetchMarketInfosByTop() {
        val top = 250
        marketKit.advancedMarketInfosSingle(top, "USD")
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.w("AAA", "marketInfo: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runFetchMarketInfosByCoinUids() {
        val coinUids = listOf("bitcoin", "ethereum", "solana", "ripple")
        val currencyCode = "USD"
        marketKit.marketInfosSingle(coinUids, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.w("AAA", "marketInfo: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runFetchMarketInfosByCategory() {
        val categoryUid = "dexes"
        val currencyCode = "USD"
        marketKit.marketInfosSingle(categoryUid, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.w("AAA", "marketInfo By Category: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle By Category Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runCoinCategoriesMarketData() {
        val currencyCode = "USD"
        marketKit.coinCategoriesSingle(currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.w("AAA", "Category: ${it.uid} marketCap: ${it.marketCap} diff24H: ${it.diff24H}")
                }
            }, {
                Log.e("AAA", "runCategoriesMarketData Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runCoinCategoryMarketPoints() {
        val categoryUid = "oracles"
        val interval = HsTimePeriod.Week1
        val currencyCode = "RUB"
        marketKit.coinCategoryMarketPointsSingle(categoryUid, interval, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.w("AAA", "Category Market Point: ${categoryUid} marketCap: ${it.marketCap} timestamp: ${it.timestamp}")
                }
            }, {
                Log.e("AAA", "runCoinCategoryMarketPoints Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runFetchPosts() {
        marketKit.postsSingle()
            .subscribeOn(Schedulers.io())
            .subscribe({ posts ->
                Log.w("AAA", "posts size ${posts.size}")
                posts.forEach {
                    Log.w("AAA", "post: ${it.source}: ${it.title} - <${it.url}>")
                }
            }, {
                Log.e("AAA", "postsSingle error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runMarketInfoOverview() {
        doMarketInfoOverview("bitcoin")
        doMarketInfoOverview("tether")
    }

    fun runMarketOverview() {
        Log.w("AAA", "doMarketOverview")
        marketKit.marketOverviewSingle("USD")
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "marketOverview global: ${it.globalMarketPoints}")
                Log.w("AAA", "marketOverview coinCategories: ${it.coinCategories}")
                Log.w("AAA", "marketOverview topPlatforms: ${it.topPlatforms}")
                Log.w("AAA", "marketOverview nft collections: ${it.nftCollections}")
            }, {
                Log.e("AAA", "marketOverview Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runNftCollections() {
        Log.w("AAA", "doCollections")
        viewModelScope.launch {
            val collections = marketKit.nftTopCollections()

            Log.w("AAA", "collections count: ${collections.size}")
            collections.firstOrNull()?.let { collection ->
                Log.w("AAA", "${collection.blockchainType}")
                Log.w("AAA", "${collection.providerUid}")
                Log.w("AAA", "${collection.name}")
                Log.w("AAA", "${collection.thumbnailImageUrl}")
                Log.w("AAA", "${collection.floorPrice}")
                Log.w("AAA", "${collection.volumes}")
                Log.w("AAA", "${collection.changes}")
            }
        }
    }

    private fun doMarketInfoOverview(coinUid: String) {
        Log.w("AAA", "doMarketInfoOverview coinUid: $coinUid")
        marketKit.marketInfoOverviewSingle(coinUid, "USD", "en")
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "marketInfoOverview: $it")
            }, {
                Log.e("AAA", "marketInfoOverview Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runGlobalMarketPoints() {
        val currencyCode = "USD"
        val timePeriod = HsTimePeriod.Day1
        marketKit.globalMarketPointsSingle(currencyCode, timePeriod)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "globalMarketPoints size: ${it.size}")
            }, {
                Log.e("AAA", "globalMarketPoints Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runGetMarketTickers() {
        val coinUid = "ethereum"
        marketKit.marketTickersSingle(coinUid)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it
                    .sortedByDescending { it.volume }
                    .forEach {
                        Log.w(
                            "AAA",
                            "getMarketTickers: ${it.marketName} rate: ${it.rate} vol: ${it.volume} base: ${it.base} target: ${it.target} tradeUrl: ${it.tradeUrl}"
                        )
                    }
            }, {
                Log.e("AAA", "getMarketTickers Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runGetMarketDefi() {
        val currencyUsd = "usd"
        marketKit.defiMarketInfosSingle(currencyUsd)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it
                    .forEach {
                        Log.w(
                            "AAA",
                            "getMarketDefi: ${it.name} tvl: ${it.tvl} tvlRank: ${it.tvlRank} tvlChange2W: ${it.tvlChange2W} chainTvls.size: ${it.chainTvls.size}"
                        )
                    }
            }, {
                Log.e("AAA", "getMarketDefi Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runTokensByBlockchainType() {
        val blockchainType = BlockchainType.Ethereum
        val coinList = marketKit.tokens(blockchainType, "eth", 30)
        Log.w("AAA", "tokensByBlockchainType ${coinList.size} coins found")
        coinList.forEach {
            Log.w("AAA", "tokensByBlockchainType code: ${it.coin.code} name: ${it.coin.name} marketCapRank: ${it.coin.marketCapRank} coinType.id: ${it.type.id}")
        }
    }

    fun runBlockchainsType() {
        val blockchains = marketKit.blockchains(listOf("bitcoin", "ethereum"))
        Log.w("AAA", "runBlockchainsType ${blockchains.size} coins found")
        blockchains.forEach {
            Log.w("AAA", "runBlockchainsType name: ${it.name}")
        }
    }

    fun runFullCoins() {
        val fullCoins = marketKit.fullCoins(listOf("bitcoin", "ethereum"))
        Log.w("AAA", "runFullCoins ${fullCoins.size} coins found")
        fullCoins.forEach {
            Log.w("AAA", "runFullCoins name: ${it.coin.name} tokens: ${it.tokens.size}")
        }
    }

    fun runTokenByTokenQuery() {
        val blockchainType = BlockchainType.Ethereum
        val coin = marketKit.token(TokenQuery(blockchainType, TokenType.Native))
        Log.w("AAA", "runTokenByTokenQuery $coin")
    }

    fun runTokensByTokenQuery() {
        val queries = listOf(
            TokenQuery(BlockchainType.Ethereum, TokenType.Native),
            TokenQuery(BlockchainType.BinanceSmartChain, TokenType.Native)
        )

        val coinsList = marketKit.tokens(queries)
        coinsList.forEach {
            Log.w("AAA", "runTokensByTokenQuery code: ${it.coin.code} name: ${it.coin.name} marketCapRank: ${it.coin.marketCapRank} coinType.id: ${it.type.id}")
        }
    }

    fun runTokensReference() {
        val coinsList = marketKit.tokens("0x11cdb42b0eb46d95f990bedd4695a6e3fa034978")
        coinsList.forEach {
            Log.w("AAA", "runTokensReference code: ${it.coin.code} name: ${it.coin.name} marketCapRank: ${it.coin.marketCapRank} coinType.id: ${it.type.id}")
        }
    }

    fun runHistoricalPrice() {
        val dateString = "01-12-2020"
        val timestamp = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            .parse(dateString)?.time?.div(1000) ?: return

        marketKit.coinHistoricalPriceSingle("bitcoin", "USD", timestamp)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "runHistoricalPrice BTC price for $dateString: $it")
            }, {
                Log.e("AAA", "coinHistoricalPriceValueSingle Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runTopPlatforms() {
        val currencyCode = "eur"
        marketKit.topPlatformsSingle(currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({ platforms ->
                platforms.forEach {
                    Log.e("AAA", "topPlatformsSingle ${it.blockchain.name} marketCap: ${it.marketCap} rank: ${it.rank}")
                }
            }, {
                Log.e("AAA", "topPlatformsSingle error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runTopPlatformMarketCapPoints() {
        val chain = "ethereum"
        val currencyCode = "rub"
        marketKit.topPlatformMarketCapPointsSingle(chain, HsTimePeriod.Day1, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({ points ->
                points.forEach {
                    Log.e("AAA", "timestamp: ${it.timestamp} marketCap: ${it.marketCap} ")
                }
            }, {
                Log.e("AAA", "topPlatformsMarketCapPointsSingle error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runTopPlatformCoinList() {
        val chain = "ethereum"
        val currencyCode = "eur"
        marketKit.topPlatformCoinListSingle(chain, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({ points ->
                points.forEach {
                    Log.e("AAA", "coin: ${it.fullCoin.coin.code} marketCap: ${it.marketCap} ")
                }
            }, {
                Log.e("AAA", "runTopPlatformCoinList error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runAnalyticsPreview() {
        val chain = "ethereum"
        marketKit.analyticsPreviewSingle(chain, listOf())
            .subscribeOn(Schedulers.io())
            .subscribe({ data ->
                Log.e("AAA", "cexVolume rank30d: ${data.cexVolume?.rank30d} points: ${data.cexVolume?.points} dexVolume rank30d: ${data.dexVolume?.rank30d} points: ${data.dexVolume?.points} ")
                Log.e("AAA", "fundsInvested: ${data.fundsInvested} holders: ${data.holders} ")
            }, {
                Log.e("AAA", "runAnalyticsPreview error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runAnalytics() {
        val coinUid = "ethereum"
        val currencyCode = "usd"
        marketKit.analyticsSingle(authToken, coinUid, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({ data ->
                Log.e("AAA", "cexVolume rank30d: ${data.cexVolume?.rank30d} points.size: ${data.cexVolume?.points?.size} transactions volume30d: ${data.transactions?.volume30d} points.size: ${data.transactions?.points?.size} ")
                Log.e("AAA", "fundsInvested: ${data.fundsInvested} holders.size: ${data.holders?.size} ")
            }, {
                Log.e("AAA", "runAnalyticsPreview error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runTokenHolders() {
        val coinUid = "uniswap"
        val blockchainUid = "ethereum"
        marketKit.tokenHoldersSingle(authToken, coinUid, blockchainUid)
            .subscribeOn(Schedulers.io())
            .subscribe({ data ->
                Log.e("AAA", "runTokenHolders count: ${data.count} url: ${data.holdersUrl} holders.size: ${data.topHolders.size} ")
                data.topHolders.forEach { holder ->
                    Log.e("AAA", "Holder: address: ${holder.address} percentage: ${holder.percentage} ")
                }
            }, {
                Log.e("AAA", "runAnalyticsPreview error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runDexLiquidityRanks() {
        val currencyCode = "usd"
        marketKit.dexLiquidityRanksSingle(authToken, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({ data ->
                data.forEach { item ->
                    Log.e("AAA", "runDexLiquidityRanks value: ${item.value} uid: ${item.uid} ")
                }
            }, {
                Log.e("AAA", "runDexLiquidityRanks error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runRevenueRanks() {
        val currencyCode = "usd"
        marketKit.revenueRanksSingle(authToken, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({ data ->
                data.forEach { item ->
                    Log.e(
                        "AAA",
                        "runRevenueRanks value1d: ${item.value1d} value7d: ${item.value7d} uid: ${item.uid} "
                    )
                }
            }, {
                Log.e("AAA", "runRevenueRanks error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runHoldersRanks() {
        val currencyCode = "usd"
        marketKit.holderRanksSingle(authToken, currencyCode)
            .subscribeOn(Schedulers.io())
            .subscribe({ data ->
                data.forEach { item ->
                    Log.e(
                        "AAA",
                        "runHoldersRanks value1d: value: ${item.value} uid: ${item.uid} "
                    )
                }
            }, {
                Log.e("AAA", "runHoldersRanks error", it)
            }).let {
                disposables.add(it)
            }
    }

    override fun onCleared() {
        disposables.clear()
    }
}
