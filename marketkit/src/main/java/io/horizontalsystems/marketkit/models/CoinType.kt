package io.horizontalsystems.marketkit.models

sealed class CoinType {
    object Bitcoin : CoinType()
    object BitcoinCash : CoinType()
    object Litecoin : CoinType()
    object Dash : CoinType()
    object Zcash : CoinType()
    object Ethereum : CoinType()
    object BinanceSmartChain : CoinType()
    class Erc20(val address: String) : CoinType()
    class Bep20(val address: String) : CoinType()
    class Bep2(val symbol: String) : CoinType()
    class Sol20(val address: String) : CoinType()
    class Unsupported(val type: String) : CoinType()

    val id: String
        get() {
            return when (this) {
                is Bitcoin -> "bitcoin"
                is BitcoinCash -> "bitcoinCash"
                is Litecoin -> "litecoin"
                is Dash -> "dash"
                is Zcash -> "zcash"
                is Ethereum -> "ethereum"
                is BinanceSmartChain -> "binanceSmartChain"
                is Erc20 -> "erc20|${address}"
                is Bep20 -> "bep20|${address}"
                is Bep2 -> "bep2|${symbol}"
                is Sol20 -> "sol20|$address"
                is Unsupported -> "unsupported|${type}"
            }
        }

    override fun equals(other: Any?): Boolean {
        return other is CoinType && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return when (this) {
            Bitcoin -> "bitcoin"
            BitcoinCash -> "bitcoinCash"
            Litecoin -> "litecoin"
            Dash -> "dash"
            Zcash -> "zcash"
            Ethereum -> "ethereum"
            BinanceSmartChain -> "binanceSmartChain"
            is Erc20 -> "erc20|${address.take(4)}...${address.takeLast(2)}"
            is Bep20 -> "bep20|${address.take(4)}...${address.takeLast(2)}"
            is Bep2 -> "bep2|$symbol"
            is Sol20 -> "sol20|${address.take(4)}...${address.takeLast(2)}"
            is Unsupported -> "unsupported|$type"
        }
    }

    companion object {
        fun getInstance(type: String, address: String?, symbol: String?): CoinType? {
            return when (type) {
                "bitcoin" -> Bitcoin
                "bitcoin-cash" -> BitcoinCash
                "litecoin" -> Litecoin
                "dash" -> Dash
                "zcash" -> Zcash
                "ethereum" -> Ethereum
                "binance-smart-chain" -> BinanceSmartChain
                "erc20" -> address?.let { Erc20(it) }
                "bep20" -> address?.let { Bep20(it) }
                "bep2" -> symbol?.let { Bep2(it) }
                "sol20" -> address?.let { Sol20(it) }
                else -> Unsupported(type)
            }
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
                    else -> Unsupported(chunks[0])
                }
            } else {
                when (chunks[0]) {
                    "erc20" -> Erc20(chunks[1])
                    "bep2" -> Bep2(chunks[1])
                    "bep20" -> Bep20(chunks[1])
                    "sol20" -> Sol20(chunks[1])
                    "unsupported" -> Unsupported(chunks.drop(1).joinToString("|"))
                    else -> Unsupported(chunks.joinToString("|"))
                }
            }
        }
    }

}
