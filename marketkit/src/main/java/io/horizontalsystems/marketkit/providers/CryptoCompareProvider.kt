package io.horizontalsystems.marketkit.providers

import io.horizontalsystems.marketkit.models.Post
import retrofit2.http.GET
import retrofit2.http.Query

class CryptoCompareProvider(val apiKey: String) {
    private val baseUrl = "https://min-api.cryptocompare.com/"
    private val newsFeeds = "cointelegraph,theblock,decrypt"
    private val excludeCategories = "Sponsored"

    private val cryptoCompareService: CryptoCompareService by lazy {
        RetrofitUtils.build(baseUrl).create(CryptoCompareService::class.java)
    }

    suspend fun postsSingle(): List<Post> {
        return retryWhenError(Throwable::class) {
            val postsResponse = cryptoCompareService.news(excludeCategories, newsFeeds, apiKey)
            postsResponse.Data.map { postItem ->
                Post(postItem.source_info["name"] ?: "", postItem.title, postItem.body, postItem.published_on, postItem.url)
            }
        }
    }

    interface CryptoCompareService {
        @GET("data/v2/news/")
        suspend fun news(
            @Query("excludedCategories") excludedCategories: String,
            @Query("feeds") feeds: String,
            @Query("api_key") apiKey: String,
        ): PostsResponse
    }

    data class PostsResponse(
        val Data: List<PostItem>
    )

    data class PostItem(
        val id: Int,
        val published_on: Long,
        val imageurl: String,
        val title: String,
        val url: String,
        val body: String,
        val source_info: Map<String, String>,
    )

}
