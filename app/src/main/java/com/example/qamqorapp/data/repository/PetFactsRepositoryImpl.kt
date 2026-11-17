package com.example.qamqorapp.data.repository

import com.example.qamqorapp.data.local.LocalPetFacts
import com.example.qamqorapp.data.remote.CatFactsApi
import com.example.qamqorapp.data.remote.DogFactsApi
import com.example.qamqorapp.domain.model.PetFact
import com.example.qamqorapp.domain.repository.PetFactsRepository
class PetFactsRepositoryImpl(
    private val catApi: CatFactsApi,
    private val dogApi: DogFactsApi,
) : PetFactsRepository {
    override suspend fun getFacts(language: String): List<PetFact> {
        if (language == "ru") {
            return LocalPetFacts.russian
        }
        val catFacts = catApi.getFacts().data.map { dto ->
            PetFact(text = dto.fact, emoji = "🐱")
        }
        val dogFacts = dogApi.getFacts().data.map { dto ->
            PetFact(text = dto.attributes.body, emoji = "🐶")
        }
        return catFacts + dogFacts
    }
}
