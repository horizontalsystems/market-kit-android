package io.horizontalsystems.marketkit.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.models.ChartType
import io.horizontalsystems.marketkit.models.PlatformType
import io.horizontalsystems.marketkit.models.TimePeriod
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(private val marketKit: MarketKit) : ViewModel() {
    private val disposables = CompositeDisposable()

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
        val coinUid = "coin-oracle"
        val currencyCode = "USD"
        val chartType = ChartType.MONTHLY
        //get stored chart info
        val storedChartInfo = marketKit.chartInfo(coinUid, currencyCode, chartType)
        Log.w("AAA", "storedChartInfo: ${storedChartInfo?.points}")

        //fetch chartInfo from API
        marketKit.getChartInfoAsync(coinUid, currencyCode, chartType)
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
        marketKit.marketInfosSingle(coinUids)
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
        marketKit.marketInfosSingle(categoryUid)
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

    fun runFetchPosts() {
        marketKit.postsSingle()
            .subscribeOn(Schedulers.io())
            .subscribe({ posts ->
                Log.w("AAA", "posts size ${posts.size}")
                posts.forEach {
                    Log.w("AAA", "post: ${it.title} - <${it.url}>")
                }
            }, {
                Log.e("AAA", "postsSingle error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    fun runMarketInfoOverview() {
        val coinUid = "bitcoin"
        val currencyCode = "USD"
        val language = "en"
        marketKit.marketInfoOverviewSingle(coinUid, currencyCode, language)
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
        val timePeriod = TimePeriod.Hour24
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
                            "getMarketTickers: ${it.marketName} rate: ${it.rate} vol: ${it.volume} base: ${it.base} target: ${it.target}"
                        )
                    }
            }, {
                Log.e("AAA", "getMarketTickers Error", it)
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

    override fun onCleared() {
        disposables.clear()
    }
}
