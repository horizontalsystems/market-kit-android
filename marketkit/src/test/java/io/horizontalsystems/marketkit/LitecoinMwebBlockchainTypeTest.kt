package io.horizontalsystems.marketkit

import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LitecoinMwebBlockchainTypeTest {

    @Test
    fun uidRoundTrip() {
        assertEquals("litecoin-mweb", BlockchainType.LitecoinMweb.uid)
        assertEquals(BlockchainType.LitecoinMweb, BlockchainType.fromUid("litecoin-mweb"))
        assertEquals("litecoinMweb", BlockchainType.LitecoinMweb.toString())
    }

    @Test
    fun isASeparateChainFromLitecoin() {
        assertNotEquals(BlockchainType.Litecoin, BlockchainType.LitecoinMweb)
        assertEquals(BlockchainType.Litecoin, BlockchainType.fromUid("litecoin"))
    }

    @Test
    fun nativeTokenQuery() {
        val query = TokenQuery(BlockchainType.LitecoinMweb, TokenType.Native)
        assertEquals("litecoin-mweb|native", query.id)
        assertEquals(query, TokenQuery.fromId("litecoin-mweb|native"))
    }
}
