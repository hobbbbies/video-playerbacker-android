package com.example.video_playbacker_android.network

import com.example.video_playbacker_android.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private const val YT_URL = "https://www.googleapis.com/youtube/v3/"
private const val NETWORK_TIMEOUT = 30L

// Using ignoreUnknownKeys = true so the app doesn't crash if YouTube adds new fields
private val networkJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
    .readTimeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
    .build()
private val ytRetrofit = Retrofit.Builder()
    .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
    .baseUrl(YT_URL)
    .client(okHttpClient)
    .build()

private val backendRetrofit = Retrofit.Builder()
    .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BuildConfig.BACKEND_URL)
    .client(okHttpClient)
    .build()

interface YoutubeDataApiService {
    @GET("search")
    suspend fun search(
        @Query("key") key: String,
        @Query("q") q: String,
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 25,
        @Query("type") type: String = "video"
    ): VideoSearchResponse
}

object YoutubeDataApi {
    val retrofitService: YoutubeDataApiService by lazy {
        ytRetrofit.create(YoutubeDataApiService::class.java)
    }
}

interface PythonApiService {
    @POST("beats")
    suspend fun beats(
        @Query("url") url: String
    ): BeatsResponse
}

object PythonApi {
    val retrofitService: PythonApiService by lazy {
        backendRetrofit.create(PythonApiService::class.java)
    }
}