package io.horizontalsystems.marketkit.managers

import io.horizontalsystems.marketkit.models.Post
import io.horizontalsystems.marketkit.providers.CryptoCompareProvider

class PostManager(
    private val provider: CryptoCompareProvider
) {
    suspend fun postsSingle(): List<Post> {
        return provider.postsSingle()
    }
}
