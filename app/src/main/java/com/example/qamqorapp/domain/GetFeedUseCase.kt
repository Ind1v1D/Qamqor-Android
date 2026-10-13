package com.example.qamqorapp.domain

import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import kotlinx.coroutines.flow.Flow

/**
 * The "recipe" from the lecture: the feed screen never calls the repository
 * directly, it asks this use case for the feed. That keeps the filtering rules in
 * one named place and lets the ViewModel stay a thin state machine.
 */
class GetFeedUseCase(
    private val repository: PostRepository,
) {
    operator fun invoke(
        status: PostStatus?,
        species: Species?,
        district: District?,
        query: String,
    ): Flow<List<Post>> = repository.feed(status, species, district, query)
}
