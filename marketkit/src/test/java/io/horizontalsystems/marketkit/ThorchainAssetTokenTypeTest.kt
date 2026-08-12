package io.horizontalsystems.marketkit

import io.horizontalsystems.marketkit.models.TokenType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies that the 3 THORChain Secured Asset denoms newly seeded into
 * initial_coins_list reconstruct to TokenType.ThorchainAsset from the stored
 * (type, reference) fields, and that they round-trip through id / fromId.
 */
class ThorchainAssetTokenTypeTest {

    private val denoms = listOf(
        "btc-btc",
        "eth-eth",
        "eth-usdc-0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
    )

    @Test
    fun fromType_resolvesToThorchainAsset() {
        for (denom in denoms) {
            assertEquals(
                TokenType.ThorchainAsset(denom),
                TokenType.fromType("thorchain", denom)
            )
        }
    }

    @Test
    fun id_hasThorchainPrefix() {
        for (denom in denoms) {
            assertEquals("thorchain:$denom", TokenType.ThorchainAsset(denom).id)
        }
    }

    @Test
    fun id_roundTripsThroughFromId() {
        for (denom in denoms) {
            val original = TokenType.ThorchainAsset(denom)
            assertEquals(original, TokenType.fromId(original.id))
        }
    }
}
