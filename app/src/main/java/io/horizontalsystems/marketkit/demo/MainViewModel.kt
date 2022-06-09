package io.horizontalsystems.marketkit.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.horizontalsystems.marketkit.models.PlatformType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(private val marketKit: MarketKit) : ViewModel() {
    private val disposables = CompositeDisposable()

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
        val interval = HsTimePeriod.Month1
        //get stored chart info
        val storedChartInfo = marketKit.chartInfo(coinUid, currencyCode, interval)
        Log.w("AAA", "storedChartInfo: ${storedChartInfo?.points}")

        //fetch chartInfo from API
        marketKit.getChartInfoAsync(coinUid, currencyCode, interval)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.w("AAA", "fetchChartInfo: ${it.points}")
            }, {
                Log.e("AAA", "fetchChartInfo Error", it)

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
            Log.w("AAA", "Coin ${it.coin.code}, ${it.coin.name}, platforms: ${it.platforms}")
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
        marketKit.coinCategoryMarketPointsSingle(categoryUid, interval)
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

    fun runCollections() {
        Log.w("AAA", "doCollections")
        viewModelScope.launch {
            val collections = marketKit.nftCollections()

            Log.w("AAA", "collections count: ${collections.size}")
            Log.w("AAA", "first collection: ${collections.firstOrNull()}")
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

    fun runPlatformCoinsByPlatformType() {
        val platformType = PlatformType.Ethereum
        val filter = "eth"
        val coinList = marketKit.platformCoins(platformType, filter, 30)
        coinList.forEach {
            Log.w("AAA", "getPlatformCoinsByPlatformType code: ${it.code} name: ${it.name} marketCapRank: ${it.coin.marketCapRank} coinType.id: ${it.coinType.id}", )
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
                    Log.e("AAA", "topPlatformsSingle ${it.name} marketCap: ${it.marketCap} rank: ${it.rank}")
                }
            }, {
                Log.e("AAA", "topPlatformsSingle error", it)
            }).let {
                disposables.add(it)
            }
    }

    fun runTopPlatformsMarketCapPoints() {
        val chain = "ethereum"
        marketKit.topPlatformsMarketCapPointsSingle(chain)
            .subscribeOn(Schedulers.io())
            .subscribe({ points ->
                points.forEach {
                    Log.e("AAA", "date: ${it.date} marketCap: ${it.marketCap} ")
                }
            }, {
                Log.e("AAA", "topPlatformsMarketCapPointsSingle error", it)
            }).let {
                disposables.add(it)
            }
    }

    override fun onCleared() {
        disposables.clear()
    }
}
