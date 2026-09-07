package com.example.video_playbacker_android.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

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
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .build()

interface YoutubeDataAPIService {
    @GET("search")
    suspend fun search(
        @Query("key") key: String,
        @Query("q") q: String,
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 25,
        @Query("type") type: String = "video"
    ): VideoSearchResponse
}

object YoutubeDataAPI {
    val retrofitService: YoutubeDataAPIService by lazy {
        retrofit.create(YoutubeDataAPIService::class.java)
    }
}

interface pythonAPIService {
    @POST("beats")
    suspend fun beats(
        @Query("url") url: String
    ): BeatsResponse
}

