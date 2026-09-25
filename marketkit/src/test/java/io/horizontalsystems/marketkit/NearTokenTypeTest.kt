package io.horizontalsystems.marketkit

import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenEntity
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NearTokenTypeTest {

    private val usdc = TokenType.Nep141("17208628f84f5d6ad33f0da3bbbeb27ffcb398eac501a31bd6ad2011e36133a1")
    private val wnear = TokenType.Nep141("wrap.near")

    @Test
    fun idRoundTrip() {
        assertEquals("near-protocol:wrap.near", wnear.id)
        assertEquals(wnear, TokenType.fromId(wnear.id))
        assertEquals(usdc, TokenType.fromId(usdc.id))
    }

    @Test
    fun valuesMatchBackendShape() {
        // backend: {"type":"near-protocol","blockchain_uid":"near-protocol","address":"wrap.near"}
        assertEquals(TokenType.Value("near-protocol", "wrap.near"), wnear.values)
        assertEquals(wnear, TokenType.fromType("near-protocol", "wrap.near"))
        assertTrue(TokenType.fromType("near-protocol", "") is TokenType.Unsupported)
    }

    @Test
    fun nearBlockchainType() {
        assertEquals("near-protocol", BlockchainType.Near.uid)
        assertEquals(BlockchainType.Near, BlockchainType.fromUid("near-protocol"))
        assertEquals("near-protocol|near-protocol:wrap.near", TokenQuery(BlockchainType.Near, wnear).id)
        assertEquals(TokenQuery(BlockchainType.Near, TokenType.Native), TokenQuery.fromId("near-protocol|native"))
    }

    @Test
    fun normalizeMovesNativeNearNextToItsTokens() {
        val backend = listOf(
            TokenEntity("near", "near", "native", null, ""),
            TokenEntity("wrapped-near", "near-protocol", "near-protocol", 24, "wrap.near"),
            TokenEntity("bitcoin", "bitcoin", "native", 8, ""),
        )
        val result = CoinSyncer.normalizeNear(backend)

        assertEquals(TokenEntity("near", "near-protocol", "native", 24, ""), result[0])
        assertEquals(backend.drop(1), result.drop(1))
    }

    @Test
    fun normalizeIsANoOpOnceTheBackendIsFixed() {
        val fixed = listOf(
            TokenEntity("near", "near-protocol", "native", 24, ""),
            TokenEntity("wrapped-near", "near-protocol", "near-protocol", 24, "wrap.near"),
        )
        assertEquals(fixed, CoinSyncer.normalizeNear(fixed))
        // a leftover legacy row next to the fixed one is dropped instead of duplicated
        assertEquals(fixed, CoinSyncer.normalizeNear(fixed + TokenEntity("near", "near", "native", null, "")))
    }
}
