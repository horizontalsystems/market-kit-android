package io.horizontalsystems.marketkit

import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TokenTypeSerializationTest {

    private val tokenTypes = listOf(
        TokenType.Native,
        TokenType.Derived(TokenType.Derivation.Bip84),
        TokenType.AddressTyped(TokenType.AddressType.Type145),
        TokenType.Eip20("0x0000000000000000000000000000000000000001"),
        TokenType.Spl("So11111111111111111111111111111111111111112"),
        TokenType.Jetton("EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
        TokenType.Asset("USDC", "GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN"),
        TokenType.ZanoAsset("aaaabbbbccccdddd"),
        TokenType.ThorchainAsset("rune"),
        TokenType.Unsupported("unsupported", "reference"),
    )

    // Array polymorphism keeps the class discriminator out of the object body, so that
    // subclasses with a "type" property (AddressTyped) do not collide with it.
    private val json = Json { useArrayPolymorphism = true }

    @Test
    fun everySubclassRoundTripsPolymorphically() {
        tokenTypes.forEach { tokenType ->
            val encoded = json.encodeToString(TokenType.serializer(), tokenType)
            assertEquals(tokenType, json.decodeFromString(TokenType.serializer(), encoded))
        }
    }
}
