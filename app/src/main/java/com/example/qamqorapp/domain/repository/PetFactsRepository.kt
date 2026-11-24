package com.example.qamqorapp.domain.repository
import com.example.qamqorapp.domain.model.PetFact
interface PetFactsRepository {
    suspend fun getFacts(language: String): List<PetFact>
}
