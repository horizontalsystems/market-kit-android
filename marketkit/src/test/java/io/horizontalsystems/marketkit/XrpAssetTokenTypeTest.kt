package io.horizontalsystems.marketkit

import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XrpAssetTokenTypeTest {

    private val rlusd = TokenType.XrpAsset("524C555344000000000000000000000000000000", "rMxCKbEDwqr76QuheSUMdEGf4B9xJ8m5De")
    private val usd = TokenType.XrpAsset("USD", "rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq")

    @Test
    fun idRoundTrip() {
        assertEquals("xrp:524C555344000000000000000000000000000000-rMxCKbEDwqr76QuheSUMdEGf4B9xJ8m5De", rlusd.id)
        assertEquals(rlusd, TokenType.fromId(rlusd.id))
        assertEquals("xrp:USD-rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq", usd.id)
        assertEquals(usd, TokenType.fromId(usd.id))
    }

    @Test
    fun valuesMatchBackendShape() {
        assertEquals(TokenType.Value("xrp", "USD-rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq"), usd.values)
        assertEquals(usd, TokenType.fromType("xrp", "USD-rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq"))
    }

    @Test
    fun malformedReferenceIsUnsupported() {
        assertTrue(TokenType.fromType("xrp", "rMxCKbEDwqr76QuheSUMdEGf4B9xJ8m5De") is TokenType.Unsupported)
        assertTrue(TokenType.fromType("xrp", "USD-") is TokenType.Unsupported)
        assertTrue(TokenType.fromType("xrp", "") is TokenType.Unsupported)
    }

    @Test
    fun rippleBlockchainType() {
        assertEquals("xrp", BlockchainType.Xrp.uid)
        assertEquals(BlockchainType.Xrp, BlockchainType.fromUid("xrp"))
        assertEquals("xrp|xrp:USD-rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq", TokenQuery(BlockchainType.Xrp, usd).id)
        assertEquals(TokenQuery(BlockchainType.Xrp, TokenType.Native), TokenQuery.fromId("xrp|native"))
    }
}
