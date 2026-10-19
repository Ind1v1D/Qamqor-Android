package com.example.qamqorapp.ui.profile

import android.util.Log
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
class ProfileViewModel(
    private val repository: PostRepository,
    private val settings: com.example.qamqorapp.data.SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<com.example.qamqorapp.data.ThemeMode> = settings.themeMode

    val language: StateFlow<String> = settings.language

    val simulateError: StateFlow<Boolean> = repository.shouldFail

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

    fun setTheme(mode: com.example.qamqorapp.data.ThemeMode) {
        settings.setTheme(mode)
    }

    fun setLanguage(tag: String, context: android.content.Context) = viewModelScope.launch {
        settings.setLanguage(tag)
        repository.reseedDemo(context)
    }

    fun setSimulateError(value: Boolean) {
        repository.setSimulateError(value)
    }

    init {
        Log.d(TAG, "init")
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
    }

    private companion object {
        const val TAG = "ProfileViewModel"
    }
}