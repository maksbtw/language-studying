package com.example.projekt.api

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

object RetrofitClient {
    private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"

    private val moshi = Moshi.Builder()
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("{word}")
    suspend fun getWordInfo(@Path("word") word: String): List<WordInfoResponse>
}