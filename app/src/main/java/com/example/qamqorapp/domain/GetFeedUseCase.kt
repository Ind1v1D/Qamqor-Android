package com.example.qamqorapp.domain

import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import kotlinx.coroutines.flow.Flow
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
