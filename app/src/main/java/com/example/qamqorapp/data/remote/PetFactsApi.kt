package com.example.qamqorapp.data.remote
import kotlinx.serialization.Serializable
import retrofit2.http.GET
@Serializable
data class CatFactDto(
    val fact: String,
)

@Serializable
data class CatFactsResponse(
    val data: List<CatFactDto>,
)

interface CatFactsApi {
    @GET("facts?limit=5")
    suspend fun getFacts(): CatFactsResponse
}

@Serializable
data class DogFactAttributes(
    val body: String,
)

@Serializable
data class DogFactDto(
    val attributes: DogFactAttributes,
)

@Serializable
data class DogFactsResponse(
    val data: List<DogFactDto>,
)

interface DogFactsApi {
    @GET("api/v2/facts?limit=5")
    suspend fun getFacts(): DogFactsResponse
}
