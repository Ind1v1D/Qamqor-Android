package com.example.qamqorapp.domain

import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository

class ToggleFavoriteUseCase(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(post: Post) = repository.toggleFavorite(post)
}
