package io.horizontalsystems.marketkit.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.models.ChartType
import io.horizontalsystems.marketkit.models.TimePeriod
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(private val marketKit: MarketKit) : ViewModel() {
    private val disposables = CompositeDisposable()

    fun run() {
        syncCoins()
        fetchMarketInfos()
        fetchMarketInfos(listOf("bitcoin", "ethereum", "solana", "ripple"))
        fetchCoinsByCategory("dexes")
        fetchPosts()
        marketInfoOverview("bitcoin", "EUR", "en")
        getChartInfo("coin-oracle", "USD", ChartType.MONTHLY)
        globalMarketPoints("USD", TimePeriod.Hour24)
        getMarketTickers("bitcoin")
    }

    private fun getChartInfo(coinUid: String, currencyCode: String, chartType: ChartType) {
        //get stored chart info
        val storedChartInfo = marketKit.chartInfo(coinUid, currencyCode, chartType)
        Log.e("AAA", "storedChartInfo: ${storedChartInfo?.points}")

        //fetch chartInfo from API
        marketKit.getChartInfoAsync(coinUid, currencyCode, chartType)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e("AAA", "fetchChartInfo: ${it.points}")
            }, {
                Log.e("AAA", "fetchChartInfo Error", it)

            })
            .let {
                disposables.add(it)
            }
    }

    private fun syncCoins() {
        marketKit.sync()
        marketKit.refreshCoinPrices("USD")

        marketKit.coinPriceMapObservable(listOf("bitcoin", "ethereum", "solana"), "USD")
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e("AAA", "coinPrices: ${it.size}")
                it.forEach {
                    Log.e("AAA", "coinPrice ${it.key}: ${it.value}")
                }
            }, {
                Log.e("AAA", "coinPriceMapObservable error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun fetchMarketInfos(top: Int = 250) {
        marketKit.advancedMarketInfosSingle(top)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.e("AAA", "marketInfo: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun fetchCoinsByCategory(categoryUid: String) {
        marketKit.marketInfosSingle(categoryUid)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.e("AAA", "marketInfo By Category: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle By Category Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun fetchMarketInfos(coinUids: List<String>, ) {
        marketKit.marketInfosSingle(coinUids)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.e("AAA", "marketInfo: $it")
                }
            }, {
                Log.e("AAA", "marketInfosSingle Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun fetchPosts() {
        marketKit.postsSingle()
            .subscribeOn(Schedulers.io())
            .subscribe({ posts ->
                Log.e("AAA", "posts size ${posts.size}")
                posts.forEach {
                    Log.e("AAA", "post: ${it.title} - <${it.url}>")
                }
            }, {
                Log.e("AAA", "postsSingle error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun marketInfoOverview(coinUid: String, currencyCode: String, language: String) {
        marketKit.marketInfoOverviewSingle(coinUid, currencyCode, language)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e("AAA", "marketInfoOverview: $it")
            }, {
                Log.e("AAA", "marketInfoOverview Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun globalMarketPoints(currencyCode: String, timePeriod: TimePeriod) {
        marketKit.globalMarketPointsSingle(currencyCode, timePeriod)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e("AAA", "globalMarketPoints size: ${it.size}")
            }, {
                Log.e("AAA", "globalMarketPoints Error", it)
            })
            .let {
                disposables.add(it)
            }
    }

    private fun getMarketTickers(coinUid: String) {
        marketKit.marketTickersSingle(coinUid)
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.forEach {
                    Log.e("AAA", "getMarketTickers: ${it.marketName} rate: ${it.rate} vol: ${it.volume} base: ${it.base} target: ${it.target}")
                }
            }, {
                Log.e("AAA", "getMarketTickers Error", it)

            })
            .let {
                disposables.add(it)
            }
    }

    override fun onCleared() {
        disposables.clear()
    }
}
