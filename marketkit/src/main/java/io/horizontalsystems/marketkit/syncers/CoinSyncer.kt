package io.horizontalsystems.marketkit.syncers

import android.util.Log
import io.horizontalsystems.marketkit.models.BlockchainEntity
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.TokenEntity
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.SyncerStateDao
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class CoinSyncer(
    private val hsProvider: HsProvider,
    private val storage: CoinStorage,
    private val syncerStateDao: SyncerStateDao
) {
    private val keyCoinsLastSyncTimestamp = "coin-syncer-coins-last-sync-timestamp"
    private val keyBlockchainsLastSyncTimestamp = "coin-syncer-blockchains-last-sync-timestamp"
    private val keyTokensLastSyncTimestamp = "coin-syncer-tokens-last-sync-timestamp"

    private var disposable: Disposable? = null

    val fullCoinsUpdatedObservable = PublishSubject.create<Unit>()

    fun sync(coinsTimestamp: Int, blockchainsTimestamp: Int, tokensTimestamp: Int) {
        val lastCoinsSyncTimestamp = syncerStateDao.get(keyCoinsLastSyncTimestamp)?.toInt() ?: 0
        val coinsOutdated = lastCoinsSyncTimestamp != coinsTimestamp

        val lastBlockchainsSyncTimestamp = syncerStateDao.get(keyBlockchainsLastSyncTimestamp)?.toInt() ?: 0
        val blockchainsOutdated = lastBlockchainsSyncTimestamp != blockchainsTimestamp

        val lastTokensSyncTimestamp = syncerStateDao.get(keyTokensLastSyncTimestamp)?.toInt() ?: 0
        val tokensOutdated = lastTokensSyncTimestamp != tokensTimestamp

        if (!coinsOutdated && !blockchainsOutdated && !tokensOutdated) return

        val coinsSingle = if (coinsOutdated) hsProvider.allCoinsSingle() else Single.just(listOf())
        val blockchainsSingle = if (blockchainsOutdated) hsProvider.allBlockchainsSingle() else Single.just(listOf())
        val tokensSingle = if (tokensOutdated) hsProvider.allTokensSingle() else Single.just(listOf())

        disposable = Single.zip(coinsSingle, blockchainsSingle, tokensSingle) { r1, r2, r3 -> Triple(r1, r2, r3) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ coinsData ->
                handleFetched(coinsData.first, coinsData.second, coinsData.third)
                saveLastSyncTimestamps(coinsTimestamp, blockchainsTimestamp, tokensTimestamp)
            }, {
                Log.e("CoinSyncer", "sync() error", it)
            })
    }

    fun stop() {
        disposable?.dispose()
        disposable = null
    }

    private fun handleFetched(coins: List<Coin>, blockchainEntities: List<BlockchainEntity>, tokenEntities: List<TokenEntity>) {
        storage.update(coins, blockchainEntities, tokenEntities)
        fullCoinsUpdatedObservable.onNext(Unit)
    }

    private fun saveLastSyncTimestamps(coins: Int, blockchains: Int, tokens: Int) {
        syncerStateDao.save(keyCoinsLastSyncTimestamp, coins.toString())
        syncerStateDao.save(keyBlockchainsLastSyncTimestamp, blockchains.toString())
        syncerStateDao.save(keyTokensLastSyncTimestamp, tokens.toString())
    }

}
