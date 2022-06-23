package io.horizontalsystems.marketkit.models

sealed class TokenType {

    object Native : TokenType()
    class Eip20(val address: String) : TokenType()
    class Bep2(val symbol: String) : TokenType()
    class Unsupported(val type: String, val reference: String?) : TokenType()

    val id: String
        get() = when (this) {
            Native -> "native"
            is Eip20 -> listOf("eip20", address).joinToString(":")
            is Bep2 -> listOf("bep2", symbol).joinToString(":")
            is Unsupported -> if (reference != null) {
                listOf("unsupported", type, reference).joinToString(":")
            } else {
                listOf("unsupported", type).joinToString(":")
            }
        }

    val values: Value
        get() = when (this) {
            is Native -> Value("native", null)
            is Eip20 -> Value("eip20", address)
            is Bep2 -> Value("bep2", symbol)
            is Unsupported -> Value(type, reference)
        }

    data class Value(
        val type: String,
        val reference: String?
    )

    companion object {

        fun fromType(type: String, reference: String? = null): TokenType {
            when (type) {
                "native" -> return Native

                "eip20" -> {
                    if (reference != null) {
                        return Eip20(reference)
                    }
                }

                "bep2" -> {
                    if (reference != null) {
                        return Bep2(reference)
                    }
                }

                else -> {}
            }

            return Unsupported(type, reference)
        }

        fun fromId(id: String): TokenType? {
            val chunks = id.split(":")

            when (chunks.size) {
                1 -> if (chunks[0] == "native") {
                    return Native
                }

                2 -> when (chunks[0]) {
                    "eip20" -> Eip20(chunks[1])
                    "bep2" -> Bep2(chunks[1])
                    "unsupported" -> Unsupported(chunks[1], null)
                    else -> {}
                }

                3 -> if (chunks[0] == "unsupported") {
                    return Unsupported(chunks[1], chunks[2])
                }

                else -> {}
            }

            return null
        }

    }

}
