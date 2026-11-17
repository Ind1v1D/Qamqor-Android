package com.example.qamqorapp.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
object PetFactsClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val converter = json.asConverterFactory("application/json".toMediaType())

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(converter)
        .build()

    val catFactsApi: CatFactsApi = retrofit("https://catfact.ninja/")
        .create(CatFactsApi::class.java)

    val dogFactsApi: DogFactsApi = retrofit("https://dogapi.dog/")
        .create(DogFactsApi::class.java)
}
