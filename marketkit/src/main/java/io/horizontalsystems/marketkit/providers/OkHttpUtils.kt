package io.horizontalsystems.marketkit.providers

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpUtils {

    val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    fun get(url: String, cachedVersionId: String?): Response {
        val requestBuilder = Request.Builder().url(url)
        cachedVersionId?.let {
            requestBuilder.header("If-None-Match", it)
        }

        return client.newCall(requestBuilder.build()).execute()
    }

}
