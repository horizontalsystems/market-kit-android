package io.horizontalsystems.marketkit.syncers

import android.util.Log
import io.horizontalsystems.marketkit.SyncInfo
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.SyncerStateDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CoinSyncer(
    private val hsProvider: HsProvider,
    private val storage: CoinStorage,
    private val syncerStateDao: SyncerStateDao
) {
    private val BACKEND_SYNC_DISABLED = true

    private val keyCoinsLastSyncTimestamp = "coin-syncer-coins-last-sync-timestamp"
    private val keyBlockchainsLastSyncTimestamp = "coin-syncer-blockchains-last-sync-timestamp"
    private val keyTokensLastSyncTimestamp = "coin-syncer-tokens-last-sync-timestamp"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    private val _fullCoinsUpdatedObservable = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val fullCoinsUpdatedObservable: SharedFlow<Unit> = _fullCoinsUpdatedObservable.asSharedFlow()

    fun sync(coinsTimestamp: Long, blockchainsTimestamp: Long, tokensTimestamp: Long) {
        // TEMP (xrp-chain): backend sync disabled so the XRPL token rows preset in
        // initial_coins_list (uid `ripple`, CURRENCY-ISSUER references) are not overwritten by the
        // backend's `xrp` rows. Remove once the backend serves XRPL tokens in that shape.
        if (BACKEND_SYNC_DISABLED) return

        val lastCoinsSyncTimestamp = syncerStateDao.get(keyCoinsLastSyncTimestamp)?.toLong() ?: 0
        val coinsOutdated = lastCoinsSyncTimestamp != coinsTimestamp

        val lastBlockchainsSyncTimestamp = syncerStateDao.get(keyBlockchainsLastSyncTimestamp)?.toLong() ?: 0
        val blockchainsOutdated = lastBlockchainsSyncTimestamp != blockchainsTimestamp

        val lastTokensSyncTimestamp = syncerStateDao.get(keyTokensLastSyncTimestamp)?.toLong() ?: 0
        val tokensOutdated = lastTokensSyncTimestamp != tokensTimestamp

        if (!coinsOutdated && !blockchainsOutdated && !tokensOutdated) return

        job?.cancel()
        job = scope.launch {
            try {
                val (coins, blockchains, tokens) = coroutineScope {
                    val coins = async { hsProvider.allCoinsSingle().map { coinEntity(it) } }
                    val blockchains = async { hsProvider.allBlockchainsSingle().map { blockchainEntity(it) } }
                    val tokens = async { hsProvider.allTokensSingle().map { tokenEntity(it) } }
                    Triple(coins.await(), blockchains.await(), tokens.await())
                }
                handleFetched(coins, blockchains, tokens)
                saveLastSyncTimestamps(coinsTimestamp, blockchainsTimestamp, tokensTimestamp)
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("CoinSyncer", "sync() error", e)
            }
        }
    }

    private fun coinEntity(response: CoinResponse): Coin =
        Coin(
            response.uid,
            response.name,
            response.code.uppercase(),
            response.market_cap_rank,
            response.coingecko_id,
            response.image
        )

    private fun blockchainEntity(response: BlockchainResponse): BlockchainEntity =
        BlockchainEntity(response.uid, response.name, response.url)

    private fun tokenEntity(response: TokenResponse): TokenEntity =
        TokenEntity(
            response.coin_uid,
            response.blockchain_uid,
            response.type,
            response.decimals,

            when (response.type) {
                "eip20" -> response.address
                "spl" -> response.address
                else -> response.address
            } ?: ""
        )

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun handleFetched(coins: List<Coin>, blockchainEntities: List<BlockchainEntity>, tokenEntities: List<TokenEntity>) {
        storage.update(coins, blockchainEntities, transform(tokenEntities))
        _fullCoinsUpdatedObservable.tryEmit(Unit)
    }

    private fun transform(tokenEntities: List<TokenEntity>): List<TokenEntity> {
        val derivationReferences = TokenType.Derivation.values().map { it.name }
        val addressTypes = TokenType.AddressType.values().map { it.name }

        var result = tokenEntities
        result = transform(
            result,
            BlockchainType.Bitcoin.uid,
            "derived",
            derivationReferences
        )
        result = transform(
            result,
            BlockchainType.Litecoin.uid,
            "derived",
            derivationReferences
        )
        result = transform(
            result,
            BlockchainType.BitcoinCash.uid,
            "address_type",
            addressTypes
        )

        return result
    }

    private fun transform(
        tokenEntities: List<TokenEntity>,
        blockchainUid: String,
        transformedType: String,
        references: List<String>
    ): List<TokenEntity> {
        val tokenEntitiesMutable = tokenEntities.toMutableList()
        val indexOfFirst = tokenEntitiesMutable.indexOfFirst {
            it.blockchainUid == blockchainUid
        }
        if (indexOfFirst != -1) {
            val tokenEntity = tokenEntitiesMutable.removeAt(indexOfFirst)
            val entities = references.map {
                tokenEntity.copy(type = transformedType, reference = it)
            }
            tokenEntitiesMutable.addAll(entities)
        }
        return tokenEntitiesMutable
    }

    private fun saveLastSyncTimestamps(coins: Long, blockchains: Long, tokens: Long) {
        syncerStateDao.save(keyCoinsLastSyncTimestamp, coins.toString())
        syncerStateDao.save(keyBlockchainsLastSyncTimestamp, blockchains.toString())
        syncerStateDao.save(keyTokensLastSyncTimestamp, tokens.toString())
    }

    fun syncInfo(): SyncInfo {
        return SyncInfo(
            coinsTimestamp = syncerStateDao.get(keyCoinsLastSyncTimestamp),
            blockchainsTimestamp = syncerStateDao.get(keyBlockchainsLastSyncTimestamp),
            tokensTimestamp = syncerStateDao.get(keyTokensLastSyncTimestamp)
        )
    }

}
