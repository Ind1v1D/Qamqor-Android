package com.example.qamqorapp.domain

import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository

/**
 * Favoriting is more than a UI toggle, so it gets its own use case: the ViewModel
 * fires the event, this class owns the rule of what "toggling" means, and the
 * repository talks to Room.
 */
class ToggleFavoriteUseCase(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(post: Post) = repository.toggleFavorite(post)
}
