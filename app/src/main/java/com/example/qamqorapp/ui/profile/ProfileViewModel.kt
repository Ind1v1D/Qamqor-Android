package com.example.qamqorapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.Profile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: Profile? = null,
    val myPosts: List<Post> = emptyList(),
    val published: Int = 0,
    val resolved: Int = 0,
    val favorites: Int = 0,
    val isLoading: Boolean = true,
)

/**
 * Profile screen: the identity row, the three counters of the stats strip, and
 * «Мои посты».
 *
 * All four numbers come from Room, so publishing, resolving or favoriting anywhere
 * in the app updates them without an explicit refresh.
 */
class ProfileViewModel(
    private val repository: PostRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> =
        combine(
            repository.profile(),
            repository.myPosts(),
            repository.mineCount(),
            repository.mineResolvedCount(),
            repository.favoriteCount(),
        ) { profile, myPosts, published, resolved, favorites ->
            ProfileUiState(
                profile = profile,
                myPosts = myPosts,
                published = published,
                resolved = resolved,
                favorites = favorites,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(),
        )

    fun toggleResolved(post: Post) = viewModelScope.launch {
        repository.setResolved(post.id, !post.isResolved)
    }

    fun delete(post: Post) = viewModelScope.launch {
        repository.delete(post.id)
    }

    fun toggleFavorite(post: Post) = viewModelScope.launch {
        repository.toggleFavorite(post)
    }

    /** Used by the edit dialog opened from the profile header. */
    fun saveProfile(name: String, phone: String) = viewModelScope.launch {
        repository.saveProfile(name.trim(), phone.trim())
    }
}
