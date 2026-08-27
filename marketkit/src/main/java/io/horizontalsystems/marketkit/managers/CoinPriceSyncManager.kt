package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.Scheduler
import io.horizontalsystems.marketkit.models.CoinPrice
import io.horizontalsystems.marketkit.providers.CoinPriceSchedulerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class CoinPriceKey(
    val tag: String,
    val coinUids: List<String>,
    val currencyCode: String
)

interface ICoinPriceCoinUidDataSource {
    fun allCoinUids(currencyCode: String): List<String>
}

class CoinPriceSyncManager(
    private val schedulerFactory: CoinPriceSchedulerFactory
) : CoinPriceManager.Listener, ICoinPriceCoinUidDataSource {

    private val schedulers = ConcurrentHashMap<String, Scheduler>()
    private val subjects = ConcurrentHashMap<CoinPriceKey, MutableSharedFlow<Map<String, CoinPrice>>>()
    private val observers = ConcurrentHashMap<CoinPriceKey, AtomicInteger>()

    private fun observingCoinUids(currencyCode: String): Set<String> {
        return subjects
            .filter { it.key.currencyCode == currencyCode }
            .map { it.key.coinUids }
            .flatten()
            .toSet()
    }

    private fun observingCoinUids(tag: String, currencyCode: String): Set<String> {
        return subjects
            .filter { it.key.tag == tag && it.key.currencyCode == currencyCode }
            .map { it.key.coinUids }
            .flatten()
            .toSet()
    }

    private fun needForceUpdate(key: CoinPriceKey): Boolean {
        //get set of all listening coins
        //found tokens which needed to update
        //make new key for force update

        val newCoinTypes = key.coinUids.minus(observingCoinUids(key.currencyCode))
        return newCoinTypes.isNotEmpty()
    }

    private fun getCounter(key: CoinPriceKey): AtomicInteger {
        var count = observers[key]
        if (count == null) {
            count = AtomicInteger(0)
            observers[key] = count
        }

        return count
    }

    private fun cleanUp(key: CoinPriceKey) {
        subjects[key] ?: return
        if (getCounter(key).get() > 0) return

        subjects.remove(key)

        if (subjects.none { it.key.currencyCode == key.currencyCode }) {
            schedulers[key.currencyCode]?.stop()
            schedulers.remove(key.currencyCode)
        }
    }

    private fun subject(key: CoinPriceKey): Flow<Map<String, CoinPrice>> {
        val subject: MutableSharedFlow<Map<String, CoinPrice>>
        var forceUpdate = false

        val candidate = subjects[key]
        if (candidate != null) {
            subject = candidate
        } else {                                        // create new subject
            forceUpdate = needForceUpdate(key)     // if subject has non-subscribed tokens we need force schedule

            subject = MutableSharedFlow(extraBufferCapacity = 1)
            subjects[key] = subject
        }

        if (schedulers[key.currencyCode] == null) {        // create scheduler if not exist
            val scheduler = schedulerFactory.scheduler(key.currencyCode, this)

            schedulers[key.currencyCode] = scheduler
        }

        if (forceUpdate) {                                // make request for scheduler immediately
            schedulers[key.currencyCode]?.start(true)
        }

        return subject
            .onSubscription {
                getCounter(key).incrementAndGet()
            }
            .onCompletion {
                getCounter(key).decrementAndGet()
                cleanUp(key)
            }
    }

    // ICoinPriceCoinUidDataSource
    override fun allCoinUids(currencyCode: String): List<String> {
        return observingCoinUids(currencyCode).toList()
    }

    fun coinPriceObservable(tag: String, coinUid: String, currencyCode: String): Flow<CoinPrice> {
        val key = CoinPriceKey(tag, listOf(coinUid), currencyCode)

        return subject(key).mapNotNull { coinPriceMap -> coinPriceMap[coinUid] }
    }

    fun coinPriceMapObservable(tag: String, coinUids: List<String>, currencyCode: String): Flow<Map<String, CoinPrice>> {
        val key = CoinPriceKey(tag, coinUids, currencyCode)
        return subject(key)
    }

    fun refresh(currencyCode: String) {
        schedulers[currencyCode]?.start(force = true)
    }

    //  CoinPriceManager.Listener

    override fun didUpdate(coinPriceMap: Map<String, CoinPrice>, currencyCode: String) {
        subjects.forEach { (key, subject) ->
            if (key.currencyCode == currencyCode) {
                val rates = coinPriceMap.filter { (coinUid, _) ->
                    key.coinUids.contains(coinUid)
                }
                if (rates.isNotEmpty()) {
                    subject.tryEmit(rates)
                }
            }
        }
    }
}
