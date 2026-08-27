package io.horizontalsystems.marketkit.demo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.HsPeriodType
import io.horizontalsystems.marketkit.models.HsPointTimePeriod
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainViewModel(private val marketKit: MarketKit) : ViewModel() {
    private val authToken = ""

    private val _exportDumpUri = MutableLiveData<Uri>()

    val exportDumpUri: LiveData<Uri>
        get() = _exportDumpUri

    private val _toastMessage = MutableLiveData<Event<String>>()

    // Emits a one-shot message to be shown as a toast when an operation finishes.
    val toastMessage: LiveData<Event<String>>
        get() = _toastMessage

    private fun notifySuccess(name: String) {
        _toastMessage.postValue(Event("$name: success"))
    }

    private fun notifyError(name: String) {
        _toastMessage.postValue(Event("$name: failed"))
    }

    // Runs a suspending request on IO, logs/handles the result via [onSuccess], and toasts
    // success or failure once the work is done.
    private fun <T : Any> report(name: String, request: suspend () -> T, onSuccess: (T) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                onSuccess(request())
                notifySuccess(name)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e("AAA", "$name error", e)
                notifyError(name)
            }
        }
    }

    fun runInvestments() {
        val coinUid = "ethereum"

        report("Investments", { marketKit.investmentsSingle(coinUid) }) { investments ->
            investments.forEach {
                Log.e("AAA", it.round)
            }
        }
    }

    fun runCoinReports() {
        val coinUid = "bitcoin"

        report("CoinReports", { marketKit.coinReportsSingle(coinUid) }) { reports ->
            reports.forEach {
                Log.e("AAA", it.body)
            }
        }
    }

    fun runSyncCoins() {
        val coinUids = listOf("bitcoin", "ethereum", "solana")
        val currencyCode = "USD"

        // Timestamp of the prices currently in the database. The prices observable
        // re-emits these cached values immediately (carrying the same timestamp) and
        // then emits again once the refresh has fetched fresh prices and written them
        // to the database (with a newer timestamp). Comparing against this baseline
        // lets us toast success only for the latter — an actual new DB write.
        val baselineTimestamp = marketKit.coinPriceMap(coinUids, currencyCode)
            .values.maxOfOrNull { it.timestamp } ?: 0L

        marketKit.sync()
        marketKit.refreshCoinPrices(currencyCode)

        var notified = false
        viewModelScope.launch {
            marketKit.coinPriceMapObservable("wallet", coinUids, currencyCode)
                .flowOn(Dispatchers.IO)
                .catch {
                    Log.e("AAA", "SyncCoins error", it)
                    notifyError("SyncCoins")
                }
                .collect { priceMap ->
                    Log.w("AAA", "coinPrices: ${priceMap.size}")
                    priceMap.forEach { (uid, price) ->
                        Log.w("AAA", "coinPrice $uid: $price")
                    }

                    val newestTimestamp = priceMap.values.maxOfOrNull { it.timestamp } ?: 0L
                    if (!notified && newestTimestamp > baselineTimestamp) {
                        notified = true
                        notifySuccess("SyncCoins")
                    }
                }
        }
    }

    fun runGetChartInfo() {
        val coinUid = "ethereum"
        val currencyCode = "USD"

        val time = Date().time / 1000 - TimeUnit.DAYS.toSeconds(7)

        val interval = HsPeriodType.ByStartTime(time)

        //fetch chartInfo from API
        report("GetChartInfo", { marketKit.chartPointsSingle(coinUid, currencyCode, interval) }) {
            Log.w("AAA", "fetchChartInfo: ${it}")
        }

        report("ChartStartTime", { marketKit.chartStartTimeSingle(coinUid) }) {
            Log.w("AAA", "chartStartTimeSingle: $it")
        }
    }

    fun runGetChartPointByHsTimePeriod() {
        val coinUid = "ethereum"
        val currencyCode = "USD"

        val interval = HsPointTimePeriod.Hour1

        //fetch chartInfo from API
        report("GetChartPointByHsTimePeriod", { marketKit.chartPointsSingle(coinUid, currencyCode, interval, 12) }) {
                Log.w("AAA", "runGetChartPointByHsTimePeriod: ${it}")
            }
    }

    fun runTopFullCoins() {
        val fullCoins = marketKit.topFullCoins(100)
        Log.w("AAA", "Run topFullCoins and got ${fullCoins.size} coins")
        fullCoins.forEach {
            Log.w("AAA", "Coin ${it.coin.code}, ${it.coin.name}, platforms: ${it.tokens}")
        }
        notifySuccess("TopFullCoins")
    }

    fun runFilterFullCoins() {
        val filter = "if"
        val fullCoins = marketKit.fullCoins(filter, 100)
        Log.w("AAA", "Using filter $filter and got ${fullCoins.size} coins")
        fullCoins.forEach {
            Log.w("AAA", "Coin ${it.coin.code}, ${it.coin.name}, platforms: ${it.tokens}")
        }
        notifySuccess("FilterFullCoins")
    }

    fun runFetchMarketInfosByTop() {
        val top = 10
        report("FetchMarketInfosByTop", { marketKit.advancedMarketInfosSingle(top, "USD") }) {
            it.forEach {
                Log.w("AAA", "marketInfo: $it")
                Log.w("AAA", "marketInfo categories: ${it.categoryIds}")
            }
        }
    }

    fun runFetchMarketInfosByCoinUids() {
        val coinUids = listOf("bitcoin", "ethereum", "solana", "ripple")
        val currencyCode = "USD"
        report("FetchMarketInfosByCoinUids", { marketKit.marketInfosSingle(coinUids, currencyCode) }) {
            it.forEach {
                Log.w("AAA", "marketInfo: $it")
            }
        }
    }

    fun runFetchTopCoinsMarketInfo() {
        val currencyCode = "USD"
        report("FetchTopCoinsMarketInfo", { marketKit.topCoinsMarketInfosSingle(100, currencyCode) }) {
            it.forEach {
                Log.w("AAA", "topCoinsMarketInfo: $it")
            }
        }
    }

    fun runCategories() {
        report("Categories", { marketKit.categoriesSingle() }) {
            it.forEach {
                Log.w("AAA", "Category: $it")
            }
        }
    }

    fun runFetchMarketInfosByCategory() {
        val categoryUid = "dexes"
        val currencyCode = "USD"
        report("FetchMarketInfosByCategory", { marketKit.marketInfosSingle(categoryUid, currencyCode) }) {
            it.forEach {
                Log.w("AAA", "marketInfo By Category: $it")
            }
        }
    }

    fun runCoinCategoriesMarketData() {
        val currencyCode = "USD"
        report("CoinCategoriesMarketData", { marketKit.coinCategoriesSingle(currencyCode) }) {
            it.forEach {
                Log.w("AAA", "Category: ${it.uid} marketCap: ${it.marketCap} diff24H: ${it.diff24H} topCoins: ${it.topCoins}")
            }
        }
    }

    fun runCoinCategoryMarketPoints() {
        val categoryUid = "oracles"
        val interval = HsTimePeriod.Week1
        val currencyCode = "RUB"
        report("CoinCategoryMarketPoints", { marketKit.coinCategoryMarketPointsSingle(categoryUid, interval, currencyCode) }) {
                it.forEach {
                    Log.w("AAA", "Category Market Point: ${categoryUid} marketCap: ${it.marketCap} timestamp: ${it.timestamp}")
                }
            }
    }

    fun runFetchPosts() {
        report("FetchPosts", { marketKit.postsSingle() }) { posts ->
            Log.w("AAA", "posts size ${posts.size}")
            posts.forEach {
                Log.w("AAA", "post: ${it.source}: ${it.title} - <${it.url}>")
            }
        }
    }

    fun runMarketInfoOverview() {
        doMarketInfoOverview("bitcoin")
        doMarketInfoOverview("tether")
    }

    fun runMarketOverview() {
        Log.w("AAA", "doMarketOverview")
        report("MarketOverview", { marketKit.marketOverviewSingle("USD") }) {
            Log.w("AAA", "marketOverview global: ${it.globalMarketPoints}")
            Log.w("AAA", "marketOverview coinCategories: ${it.coinCategories}")
            Log.w("AAA", "marketOverview topPlatforms: ${it.topPlatforms}")
            Log.w("AAA", "marketOverview nft collections: ${it.nftCollections}")
            Log.w("AAA", "marketOverview top pairs: ${it.topPairs}")
        }
    }

    fun runMarketGlobal() {
        Log.w("AAA", "doMarketGlobal")
        report("MarketGlobal", { marketKit.marketGlobalSingle("USD") }) {
            Log.w("AAA", "marketGlobal: $it")
        }
    }

    fun runTopPairs() {
        Log.w("AAA", "doTopPairs")
        report("TopPairs", { marketKit.topPairsSingle("USD", 1, 100) }) {
            it.forEach {
                Log.w("AAA", "TopPairs: $it")
            }
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
            notifySuccess("NftCollections")
        }
    }

    private fun doMarketInfoOverview(coinUid: String) {
        Log.w("AAA", "doMarketInfoOverview coinUid: $coinUid")
        report("MarketInfoOverview", { marketKit.marketInfoOverviewSingle(coinUid, "USD", "en", listOf("bitcoin", "ethereum", "tether"), listOf(HsTimePeriod.Week1, HsTimePeriod.Month1, HsTimePeriod.Month3)) }) {
            Log.w("AAA", "marketInfoOverview: $it")
        }
    }

    fun runGlobalMarketPoints() {
        val currencyCode = "USD"
        val timePeriod = HsTimePeriod.Day1
        report("GlobalMarketPoints", { marketKit.globalMarketPointsSingle(currencyCode, timePeriod) }) {
            Log.w("AAA", "globalMarketPoints size: ${it.size}")
        }
    }

    fun runGetMarketTickers() {
        val coinUid = "ethereum"
        report("GetMarketTickers", { marketKit.marketTickersSingle(coinUid, "USD") }) {
            it
                .sortedByDescending { it.volume }
                .forEach {
                    Log.w("AAA", "getMarketTickers: $it")
                }
        }
    }

    fun runGetMarketDefi() {
        val currencyUsd = "usd"
        report("GetMarketDefi", { marketKit.defiMarketInfosSingle(currencyUsd) }) {
            it
                .forEach {
                    Log.w(
                        "AAA",
                        "getMarketDefi: ${it.name} tvl: ${it.tvl} tvlRank: ${it.tvlRank} tvlChange2W: ${it.tvlChange2W} chainTvls.size: ${it.chainTvls.size}"
                    )
                }
        }
    }

    fun runTokensByBlockchainType() {
        val blockchainType = BlockchainType.Ethereum
        val coinList = marketKit.tokens(blockchainType, "eth", 30)
        Log.w("AAA", "tokensByBlockchainType ${coinList.size} coins found")
        coinList.forEach {
            Log.w("AAA", "tokensByBlockchainType code: ${it.coin.code} name: ${it.coin.name} marketCapRank: ${it.coin.marketCapRank} coinType.id: ${it.type.id}")
        }
        notifySuccess("TokensByBlockchainType")
    }

    fun runBlockchainsType() {
        val blockchains = marketKit.blockchains(listOf("bitcoin", "ethereum"))
        Log.w("AAA", "runBlockchainsType ${blockchains.size} coins found")
        blockchains.forEach {
            Log.w("AAA", "runBlockchainsType name: ${it.name}")
        }
        notifySuccess("BlockchainsType")
    }

    fun runAllBlockchains() {
        val blockchains = marketKit.allBlockchains()
        Log.w("AAA", "runAllBlockchains ${blockchains.size} blockchains found")
        blockchains.forEach {
            Log.w("AAA", "runAllBlockchains name: ${it.name}")
        }
        notifySuccess("AllBlockchains")
    }

    fun runFullCoins() {
        val fullCoins = marketKit.fullCoins(listOf("bitcoin", "ethereum"))
        Log.w("AAA", "runFullCoins ${fullCoins.size} coins found")
        fullCoins.forEach {
            Log.w("AAA", "runFullCoins name: ${it.coin.name} tokens: ${it.tokens.size}")
        }
        notifySuccess("FullCoins")
    }

    fun runTokenByTokenQuery() {
        val blockchainType = BlockchainType.Ethereum
        val coin = marketKit.token(TokenQuery(blockchainType, TokenType.Native))
        Log.w("AAA", "runTokenByTokenQuery $coin")
        notifySuccess("TokenByTokenQuery")
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
        notifySuccess("TokensByTokenQuery")
    }

    fun runTokensReference() {
        val coinsList = marketKit.tokens("0x11cdb42b0eb46d95f990bedd4695a6e3fa034978")
        coinsList.forEach {
            Log.w("AAA", "runTokensReference code: ${it.coin.code} name: ${it.coin.name} marketCapRank: ${it.coin.marketCapRank} coinType.id: ${it.type.id}")
        }
        notifySuccess("TokensReference")
    }

    fun runHistoricalPrice() {
        val dateString = "01-12-2020"
        val timestamp = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            .parse(dateString)?.time?.div(1000) ?: return

        report("HistoricalPrice", { marketKit.coinHistoricalPriceSingle("bitcoin", "USD", timestamp) }) {
            Log.w("AAA", "runHistoricalPrice BTC price for $dateString: $it")
        }
    }

    fun runTopPlatforms() {
        val currencyCode = "eur"
        report("TopPlatforms", { marketKit.topPlatformsSingle(currencyCode) }) { platforms ->
            platforms.forEach {
                Log.e("AAA", "topPlatformsSingle ${it.blockchain.name} marketCap: ${it.marketCap} rank: ${it.rank}")
            }
        }
    }

    fun runTopPlatformMarketCapPoints() {
        val chain = "ethereum"
        val currencyCode = "rub"
        report("TopPlatformMarketCapPoints", { marketKit.topPlatformMarketCapPointsSingle(chain, currencyCode, HsPeriodType.ByPeriod(HsTimePeriod.Month1)) }) { points ->
                points.forEach {
                    Log.e("AAA", "timestamp: ${it.timestamp} marketCap: ${it.marketCap} ")
                }
            }

        report("TopPlatformMarketCapStartTime", { marketKit.topPlatformMarketCapStartTimeSingle(chain) }) {
            Log.e("AAA", "topPlatformMarketCapStartTimeSingle: $it")
        }
    }

    fun runTopPlatformCoinList() {
        val chain = "ethereum"
        val currencyCode = "eur"
        report("TopPlatformCoinList", { marketKit.topPlatformMarketInfosSingle(chain, currencyCode) }) { points ->
            points.forEach {
                Log.e("AAA", "coin: ${it.fullCoin.coin.code} marketCap: ${it.marketCap} ")
            }
        }
    }

    fun runAnalyticsPreview() {
        val chain = "ethereum"
        report("AnalyticsPreview", { marketKit.analyticsPreviewSingle(chain, listOf()) }) { data ->
            Log.e("AAA", "cexVolume rank30d: ${data.cexVolume?.rank30d} points: ${data.cexVolume?.points} dexVolume rank30d: ${data.dexVolume?.rank30d} points: ${data.dexVolume?.points} ")
            Log.e("AAA", "fundsInvested: ${data.fundsInvested} holders: ${data.holders} holders rating: ${data.holdersRating} ")
            Log.e("AAA", "fee fee rank30d: ${data.fee?.rank30d} value30d: ${data.fee?.value30d} ")
        }
    }

    fun runAnalytics() {
        val coinUid = "uniswap"
        val currencyCode = "usd"
        report("Analytics", { marketKit.analyticsSingle(authToken, coinUid, currencyCode) }) { data ->
            Log.e("AAA", "cexVolume rank30d: ${data.cexVolume?.rank30d} points.size: ${data.cexVolume?.points?.size} transactions volume30d: ${data.transactions?.volume30d} points.size: ${data.transactions?.points?.size} ")
            Log.e("AAA", "fundsInvested: ${data.fundsInvested} holders.size: ${data.holders?.size} ")
            Log.e("AAA", "issues: ${data.issues} ")
            Log.e("AAA", "advice: ${data.technicalAdvice?.advice} ${data.technicalAdvice?.middle} ")
        }
    }

    fun runTokenHolders() {
        val coinUid = "uniswap"
        val blockchainUid = "ethereum"
        report("TokenHolders", { marketKit.tokenHoldersSingle(authToken, coinUid, blockchainUid) }) { data ->
            Log.e("AAA", "runTokenHolders count: ${data.count} url: ${data.holdersUrl} holders.size: ${data.topHolders.size} ")
            data.topHolders.forEach { holder ->
                Log.e("AAA", "Holder: address: ${holder.address} percentage: ${holder.percentage} ")
            }
        }
    }

    fun runDexLiquidityRanks() {
        val currencyCode = "usd"
        report("DexLiquidityRanks", { marketKit.dexLiquidityRanksSingle(authToken, currencyCode) }) { data ->
            data.forEach { item ->
                Log.e("AAA", "runDexLiquidityRanks value: ${item.value} uid: ${item.uid} ")
            }
        }
    }

    fun runRevenueRanks() {
        val currencyCode = "usd"
        report("RevenueRanks", { marketKit.revenueRanksSingle(authToken, currencyCode) }) { data ->
            data.forEach { item ->
                Log.e(
                    "AAA",
                    "runRevenueRanks value1d: ${item.value1d} value7d: ${item.value7d} uid: ${item.uid} "
                )
            }
        }
    }

    fun runHoldersRanks() {
        val currencyCode = "usd"
        report("HoldersRanks", { marketKit.holderRanksSingle(authToken, currencyCode) }) { data ->
            data.forEach { item ->
                Log.e(
                    "AAA",
                    "runHoldersRanks value1d: value: ${item.value} uid: ${item.uid} "
                )
            }
        }
    }

    fun runCoinsSignals() {
        val uids = listOf("bitcoin", "ethereum")
        report("CoinsSignals", { marketKit.coinsSignalsSingle(uids) }) { data ->
            data.forEach { item ->
                Log.e("AAA", "runCoinsSignals value: ${item.key} uid: ${item.value} ")
            }
        }
    }

    fun runEtfs() {
        val category = "eth"
        val currencyCode = "usd"
        report("Etfs", { marketKit.etfSingle(category, currencyCode) }) {
            it.forEach {
                Log.w("AAA", "etf: ${it.ticker} ${it.name} ${it.date} ${it.totalAssets} ${it.totalInflow} ${it.inflows}")
            }
        }
    }

    fun runEtfPoints() {
        val category = "btc"
        val currencyCode = "rub"
        val period = HsTimePeriod.Month1.value
        report("EtfPoints", { marketKit.etfPointSingle(category, currencyCode, period) }) {
            it.forEach {
                Log.w("AAA", "etfPoint: ${it.date} ${it.totalAssets} ${it.totalInflow} ${it.dailyInflow}")
            }
        }
    }

    fun runRequestVipSupport() {
        val subscriptionId = "unique_subscription_id"
        report("RequestVipSupport", { marketKit.requestVipSupport("", subscriptionId) }) {
            Log.w("AAA", "runRequestVipSupport link: ${it}")
        }
    }

    fun runFullCoinsByCoinCodes() {
        val fullCoins = marketKit.fullCoinsByCoinCodes(listOf("BTC", "ETH", "USDT", "USDC", "BUSD", "BSC-USD"))
        Log.w("AAA", "runFullCoins ${fullCoins.size} coins found")
        fullCoins.forEach {
            Log.w("AAA", "runFullCoinsByCoinCodes code: ${it.coin.code} uid: ${it.coin.uid} tokens: ${it.tokens.joinToString { it.type.id }}")
        }
        notifySuccess("FullCoinsByCoinCodes")
    }

    fun exportAsDump(applicationContext: Context) {
        val exportFileName = "dump_initial_"

        viewModelScope.launch(Dispatchers.IO) {
            val dump = marketKit.getInitialDump()
            val cacheDir = applicationContext.cacheDir
            val tempFile = File.createTempFile(exportFileName, ".txt", cacheDir)
            // Write the data to the file
            FileOutputStream(tempFile).use { outputStream ->
                outputStream.write(dump.toByteArray())
            }
            // Generate a content URI for the file using FileProvider
            val exportFileUri = FileProvider.getUriForFile(
                applicationContext,
                "io.horizontalsystems.marketkit.provider", //should be same as in Manifest: android:authorities=""
                tempFile
            )

            withContext(Dispatchers.Main) {
                _exportDumpUri.value = exportFileUri
            }
        }
    }

    fun runVault() {
        val currencyCode = "usd"
        report("Vault", { marketKit.vaultSingle("0x23878914EFE38d27C4D67Ab83ed1b93A74D4086a", currencyCode, HsTimePeriod.Month1) }) { vault ->
                Log.w("AAA", "vault: ${vault.name} ${vault.assetSymbol} ${vault.tvl} ${vault.chain} ${vault.protocolName} ${vault.apy} ${vault.rank}")
                Log.w("AAA", "vault chart data: ${vault.chart} ")
            }
    }

    fun runVaults() {
        val currencyCode = "rub"
        report("Vaults", { marketKit.vaultsSingle(currencyCode) }) {
            it.forEach {
                Log.w("AAA", "vault: ${it.rank} ${it.name} ${it.assetSymbol} ${it.assetLogo} ${it.tvl} ${it.chain} ${it.protocolName} ${it.url} ${it.apy}")
            }
        }
    }

    fun runStocks() {
        val currencyCode = "usd"
        report("Stocks", { marketKit.getStocks(currencyCode) }) {
            it.forEach {
                Log.w("AAA", "stock: ${it.name} ${it.symbol} ${it.marketPrice} ${it.priceChange}")
            }
        }
    }
}

// A container for data exposed via LiveData that represents a one-time event,
// so it isn't re-delivered (e.g. on configuration change).
class Event<out T>(private val content: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? =
        if (handled) {
            null
        } else {
            handled = true
            content
        }
}