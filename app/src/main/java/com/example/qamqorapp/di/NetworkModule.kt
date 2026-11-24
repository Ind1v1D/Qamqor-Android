package com.example.qamqorapp.di

import com.example.qamqorapp.BuildConfig
import com.example.qamqorapp.data.remote.CatFactsApi
import com.example.qamqorapp.data.remote.DogFactsApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

// Network layer: Json, one shared OkHttpClient, Retrofit instances and the APIs.
val networkModule = module {

    single { Json { ignoreUnknownKeys = true } }

    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    }

    // Cat and dog APIs have other base URLs, so they get names (qualifiers)
    single(named("cat")) {
        Retrofit.Builder()
            .baseUrl("https://catfact.ninja/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single(named("dog")) {
        Retrofit.Builder()
            .baseUrl("https://dogapi.dog/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single<CatFactsApi> { get<Retrofit>(named("cat")).create(CatFactsApi::class.java) }
    single<DogFactsApi> { get<Retrofit>(named("dog")).create(DogFactsApi::class.java) }
}
