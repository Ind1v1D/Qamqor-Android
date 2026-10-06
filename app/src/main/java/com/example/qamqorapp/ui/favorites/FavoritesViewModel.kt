package com.example.qamqorapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Favorites screen — literally the same table behind a `isFavorite = 1` filter,
 * which is why the mockup's spec says "those same PostCards, reuse the component".
 */
class FavoritesViewModel(
    private val repository: PostRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> =
        repository.favorites()
            .map { FavoritesUiState(posts = it, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FavoritesUiState(),
            )

    fun remove(post: Post) = viewModelScope.launch {
        repository.setFavorite(post.id, false)
    }
}
