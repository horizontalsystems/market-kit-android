package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class CoinType : Parcelable {
    @Parcelize
    object Bitcoin : CoinType()
    @Parcelize
    object BitcoinCash : CoinType()
    @Parcelize
    object Litecoin : CoinType()
    @Parcelize
    object Dash : CoinType()
    @Parcelize
    object Zcash : CoinType()
    @Parcelize
    object Ethereum : CoinType()
    @Parcelize
    object BinanceSmartChain : CoinType()
    @Parcelize
    object Polygon : CoinType()
    @Parcelize
    object EthereumOptimism : CoinType()
    @Parcelize
    object EthereumArbitrumOne : CoinType()
    @Parcelize
    class Erc20(val address: String) : CoinType()
    @Parcelize
    class Bep20(val address: String) : CoinType()
    @Parcelize
    class Mrc20(val address: String) : CoinType()
    @Parcelize
    class OptimismErc20(val address: String) : CoinType()
    @Parcelize
    class ArbitrumOneErc20(val address: String) : CoinType()
    @Parcelize
    class Bep2(val symbol: String) : CoinType()
    @Parcelize
    class Avalanche(val address: String) : CoinType()
    @Parcelize
    class Fantom(val address: String) : CoinType()
    @Parcelize
    class HarmonyShard0(val address: String) : CoinType()
    @Parcelize
    class HuobiToken(val address: String) : CoinType()
    @Parcelize
    class Iotex(val address: String) : CoinType()
    @Parcelize
    class Moonriver(val address: String) : CoinType()
    @Parcelize
    class OkexChain(val address: String) : CoinType()
    @Parcelize
    class Solana(val address: String) : CoinType()
    @Parcelize
    class Sora(val address: String) : CoinType()
    @Parcelize
    class Tomochain(val address: String) : CoinType()
    @Parcelize
    class Xdai(val address: String) : CoinType()
    @Parcelize
    class Unsupported(val type: String) : CoinType()

    val id: String
        get() = when (this) {
            is Bitcoin -> "bitcoin"
            is BitcoinCash -> "bitcoinCash"
            is Litecoin -> "litecoin"
            is Dash -> "dash"
            is Zcash -> "zcash"
            is Ethereum -> "ethereum"
            is BinanceSmartChain -> "binanceSmartChain"
            is Polygon -> "polygon"
            is EthereumOptimism -> "ethereumOptimism"
            is EthereumArbitrumOne -> "ethereumArbitrumOne"
            is Erc20 -> "erc20|$address"
            is Bep20 -> "bep20|$address"
            is Mrc20 -> "mrc20|$address"
            is OptimismErc20 -> "optimismErc20|$address"
            is ArbitrumOneErc20 -> "arbitrumOneErc20|$address"
            is Bep2 -> "bep2|$symbol"
            is Avalanche -> "avalanche|$address"
            is Fantom -> "fantom|$address"
            is HarmonyShard0 -> "harmonyShard0|$address"
            is HuobiToken -> "huobiToken|$address"
            is Iotex -> "iotex|$address"
            is Moonriver -> "moonriver|$address"
            is OkexChain -> "okexChain|$address"
            is Solana -> "solana|$address"
            is Sora -> "sora|$address"
            is Tomochain -> "tomochain|$address"
            is Xdai -> "xdai|$address"
            is Unsupported -> "unsupported|$type"
        }

    override fun equals(other: Any?): Boolean {
        return other is CoinType && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString() = when (this) {
        Bitcoin -> "bitcoin"
        BitcoinCash -> "bitcoinCash"
        Litecoin -> "litecoin"
        Dash -> "dash"
        Zcash -> "zcash"
        Ethereum -> "ethereum"
        BinanceSmartChain -> "binanceSmartChain"
        Polygon -> "polygon"
        EthereumOptimism -> "ethereumOptimism"
        EthereumArbitrumOne -> "ethereumArbitrumOne"
        is Erc20 -> "erc20|${address.take(4)}...${address.takeLast(2)}"
        is Bep20 -> "bep20|${address.take(4)}...${address.takeLast(2)}"
        is Mrc20 -> "mrc20|${address.take(4)}...${address.takeLast(2)}"
        is OptimismErc20 -> "optimismErc20|${address.take(4)}...${address.takeLast(2)}"
        is ArbitrumOneErc20 -> "arbitrumOneErc20|${address.take(4)}...${address.takeLast(2)}"
        is Bep2 -> "bep2|$symbol"
        is Avalanche -> "avalanche|${address.take(4)}...${address.takeLast(2)}"
        is Fantom -> "fantom|${address.take(4)}...${address.takeLast(2)}"
        is HarmonyShard0 -> "harmonyShard0|${address.take(4)}...${address.takeLast(2)}"
        is HuobiToken -> "huobiToken|${address.take(4)}...${address.takeLast(2)}"
        is Iotex -> "iotex|${address.take(4)}...${address.takeLast(2)}"
        is Moonriver -> "moonriver|${address.take(4)}...${address.takeLast(2)}"
        is OkexChain -> "okexChain|${address.take(4)}...${address.takeLast(2)}"
        is Solana -> "solana|${address.take(4)}...${address.takeLast(2)}"
        is Sora -> "sora|${address.take(4)}...${address.takeLast(2)}"
        is Tomochain -> "tomochain|${address.take(4)}...${address.takeLast(2)}"
        is Xdai -> "xdai|${address.take(4)}...${address.takeLast(2)}"
        is Unsupported -> "unsupported|$type"
    }

    companion object {
        fun getInstance(type: String, address: String?, symbol: String?) = when (type) {
            "bitcoin" -> Bitcoin
            "bitcoin-cash" -> BitcoinCash
            "litecoin" -> Litecoin
            "dash" -> Dash
            "zcash" -> Zcash
            "ethereum" -> Ethereum
            "binance-smart-chain" -> BinanceSmartChain
            "polygon" -> Polygon
            "ethereum-optimism" -> EthereumOptimism
            "ethereum-arbitrum-one" -> EthereumArbitrumOne
            "erc20" -> address?.let { Erc20(it) }
            "bep20" -> address?.let { Bep20(it) }
            "optimistic-ethereum" -> symbol?.let { OptimismErc20(it) }
            "arbitrum-one" -> symbol?.let { ArbitrumOneErc20(it) }
            "bep2" -> symbol?.let { Bep2(it) }
            "avalanche" -> address?.let { Avalanche(it) }
            "fantom" -> address?.let { Fantom(it) }
            "harmony-shard-0" -> address?.let { HarmonyShard0(it) }
            "huobi-token" -> address?.let { HuobiToken(it) }
            "iotex" -> address?.let { Iotex(it) }
            "moonriver" -> address?.let { Moonriver(it) }
            "okex-chain" -> address?.let { OkexChain(it) }
            "polygon-pos" -> address?.let { Mrc20(it) }
            "solana" -> address?.let { Solana(it) }
            "sora" -> address?.let { Sora(it) }
            "tomochain" -> address?.let { Tomochain(it) }
            "xdai" -> address?.let { Xdai(it) }
            else -> Unsupported(type)
        }

        fun fromId(id: String): CoinType {
            val chunks = id.split("|")

            return if (chunks.size == 1) {
                when (chunks[0]) {
                    "bitcoin" -> Bitcoin
                    "bitcoinCash" -> BitcoinCash
                    "litecoin" -> Litecoin
                    "dash" -> Dash
                    "zcash" -> Zcash
                    "ethereum" -> Ethereum
                    "binanceSmartChain" -> BinanceSmartChain
                    "polygon" -> Polygon
                    "ethereumOptimism" -> EthereumOptimism
                    "ethereumArbitrumOne" -> EthereumArbitrumOne
                    else -> Unsupported(chunks[0])
                }
            } else {
                when (chunks[0]) {
                    "erc20" -> Erc20(chunks[1])
                    "bep2" -> Bep2(chunks[1])
                    "bep20" -> Bep20(chunks[1])
                    "mrc20" -> Mrc20(chunks[1])
                    "optimismErc20" -> OptimismErc20(chunks[1])
                    "arbitrumOneErc20" -> ArbitrumOneErc20(chunks[1])
                    "avalanche" -> Avalanche(chunks[1])
                    "fantom" -> Fantom(chunks[1])
                    "harmony-shard-0" -> HarmonyShard0(chunks[1])
                    "huobi-token" -> HuobiToken(chunks[1])
                    "iotex" -> Iotex(chunks[1])
                    "moonriver" -> Moonriver(chunks[1])
                    "okex-chain" -> OkexChain(chunks[1])
                    "solana" -> Solana(chunks[1])
                    "sora" -> Sora(chunks[1])
                    "tomochain" -> Tomochain(chunks[1])
                    "xdai" -> Xdai(chunks[1])
                    "unsupported" -> Unsupported(chunks.drop(1).joinToString("|"))
                    else -> Unsupported(chunks.joinToString("|"))
                }
            }
        }
    }

}
