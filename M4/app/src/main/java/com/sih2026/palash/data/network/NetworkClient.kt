package com.sih2026.palash.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Centralized Retrofit & OkHttp client manager.
 * Safely handles unconfigured network state without crashing the application.
 */
object NetworkClient {

    private const val TIMEOUT_SECONDS = 10L

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        builder.addInterceptor(logging)

        builder.build()
    }

    /**
     * Returns an instance of PalashApiService if NetworkConfig has a valid URL;
     * returns null if the network layer is unconfigured / disabled for offline operation.
     */
    fun getApiService(): PalashApiService? {
        val url = NetworkConfig.baseUrl
        if (url.isNullOrBlank()) return null

        return try {
            val formattedUrl = if (url.endsWith("/")) url else "$url/"
            Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PalashApiService::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
