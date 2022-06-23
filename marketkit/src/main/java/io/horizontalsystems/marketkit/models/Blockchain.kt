package io.horizontalsystems.marketkit.models

data class Blockchain(
    val type: BlockchainType,
    val name: String
) {

    val uid: String
        get() = type.uid

}
